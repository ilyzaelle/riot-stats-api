package but.bdd.riotstatsapi.web;

import but.bdd.riotstatsapi.domain.matchdata.MatchDataDoc;
import but.bdd.riotstatsapi.domain.player.PlayerDoc;
import but.bdd.riotstatsapi.repository.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/match-data")
@Tag(name = "match-data")
public class MatchDataController {

    private final MatchDataRepository matchDataRepository;
    private final PlayerRepository playerRepository;

    public MatchDataController(MatchDataRepository matchDataRepository, PlayerRepository playerRepository) {
        this.matchDataRepository = matchDataRepository;
        this.playerRepository = playerRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MatchDataDoc>> list(
            @RequestParam(required = false) String matchId,
            @RequestParam(required = false) String platformId,
            @RequestParam(required = false) Integer queueId,
            @RequestParam(required = false) String gameVersion,
            @RequestParam(required = false) Long startTimeFrom,
            @RequestParam(required = false) Long startTimeTo,
            @RequestParam(required = false) Integer durationMin,
            @RequestParam(required = false) Integer durationMax,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "info.gameEndTimestamp,desc") String sort
    ) {
        String[] s = sort.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(s.length>1?s[1]:"desc"), s[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);

        Page<MatchDataDoc> p = matchDataRepository.findAll(pageable);
        List<MatchDataDoc> filtered = p.getContent().stream()
                .filter(d -> matchId == null || (d.getMetadata()!=null && matchId.equals(d.getMetadata().getMatchId())))
                .collect(Collectors.toList());
        return ResponseEntity.ok(new PageResponse<>(filtered, p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), sort));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MatchDataDoc body) {
        String k = body.getMetadata()!=null ? body.getMetadata().getMatchId() : null;
        if (k == null) return ResponseEntity.badRequest().body(Map.of("error","metadata.matchId is required"));
        if (matchDataRepository.findByMetadataMatchId(k).isPresent()) return ResponseEntity.status(409).body(Map.of("error","Already exists"));
        body.setMatchIdKey(k);
        return ResponseEntity.status(201).body(matchDataRepository.save(body));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Integer>> bulkUpsert(@RequestBody List<MatchDataDoc> docs) {
        int inserted=0, updated=0;
        for (MatchDataDoc d : docs) {
            String k = d.getMetadata()!=null ? d.getMetadata().getMatchId() : null;
            if (k == null) continue;
            boolean exists = matchDataRepository.findByMetadataMatchId(k).isPresent();
            d.setMatchIdKey(k);
            matchDataRepository.save(d);
            if (exists) updated++; else inserted++;
        }
        return ResponseEntity.ok(Map.of("inserted", inserted, "updated", updated, "total", inserted+updated));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<?> getByMatchId(@PathVariable String matchId) {
        return matchDataRepository.findByMetadataMatchId(matchId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found")));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> delete(@PathVariable String matchId) {
        return matchDataRepository.findByMetadataMatchId(matchId).map(md -> {
            matchDataRepository.delete(md);
            return ResponseEntity.noContent().build();
        }).orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found")));
    }

    @GetMapping("/{matchId}/players")
    public ResponseEntity<?> playersInMatch(@PathVariable String matchId) {
        return matchDataRepository.findByMetadataMatchId(matchId)
                .<ResponseEntity<?>>map(md -> {
                    List<String> puuids = md.getMetadata() != null ? md.getMetadata().getParticipants() : List.of();
                    List<PlayerDoc> players = puuids.stream()
                            .map(playerRepository::findByPuuid)
                            .flatMap(Optional::stream)
                            .toList();
                    return ResponseEntity.ok(players); // ResponseEntity<List<PlayerDoc>>
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found"))); // ResponseEntity<Map<String,String>>
    }


    @GetMapping("/participants/by-puuid/{puuid}")
    public ResponseEntity<PageResponse<MatchDataDoc>> getMatchesByPuuid(@PathVariable String puuid,
                                                                        @RequestParam(defaultValue = "0") int page,
                                                                        @RequestParam(defaultValue = "50") int size) {
        Page<MatchDataDoc> p = matchDataRepository.findAllByParticipantPuuid(puuid, PageRequest.of(page, size));
        return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), "info.gameEndTimestamp,desc"));
    }

    @GetMapping("/stats/durations")
    public ResponseEntity<Map<String, Object>> durations(@RequestParam(required = false) Integer queueId,
                                       @RequestParam(required = false) String platformId,
                                       @RequestParam(required = false) Long startTimeFrom,
                                       @RequestParam(required = false) Long startTimeTo) {
        List<DurationStatsView> docs = matchDataRepository.durationsStats(queueId, platformId, startTimeFrom, startTimeTo);
        Map<String,Object> res = new java.util.LinkedHashMap<>();
        if (docs.isEmpty()) {
            res.put("min", 0);
            res.put("max", 0);
            res.put("avg", 0.0);
        } else {
            var d = docs.get(0);
            res.put("min", d.getMin());
            res.put("max", d.getMax());
            res.put("avg", d.getAvg());
        }
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/champions")
    public ResponseEntity<List<LinkedHashMap<String, Object>>> championFrequency(@RequestParam(defaultValue = "50") int limit) {
        var views = matchDataRepository.championFrequency(Math.min(Math.max(limit,1),500));
        var res = views.stream().map(v -> {
            var m = new LinkedHashMap<String, Object>();
            m.put("championId", v.getChampionId());
            m.put("count", v.getCount());
            return m;
        }).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/winrate-by-champion")
    public ResponseEntity<List<LinkedHashMap<String, Object>>> winrateByChampion(@RequestParam(required = false) Integer queueId,
                                                                                 @RequestParam(required = false) String platformId) {
        var views = matchDataRepository.winrateByChampion(queueId, platformId);
        var res = views.stream().map(v -> {
            var m = new LinkedHashMap<String,Object>();
            m.put("championId", v.getChampionId());
            m.put("games", v.getGames());
            m.put("wins", v.getWins());
            m.put("winrate", v.getWinrate());
            return m;
        }).toList();
        return ResponseEntity.ok(res);
    }
}
