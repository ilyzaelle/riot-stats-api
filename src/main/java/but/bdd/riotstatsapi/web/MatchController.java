package but.bdd.riotstatsapi.web;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.matchdata.MatchDataDoc;
import but.bdd.riotstatsapi.domain.player.PlayerDoc;
import but.bdd.riotstatsapi.domain.matchid.MatchIdDoc;
import but.bdd.riotstatsapi.repository.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/matches")
@Tag(name = "matches")
public class MatchController {

    private final MatchRepository repo;
    private final PlayerRepository playerRepository;

    public MatchController(MatchRepository repo,
                           PlayerRepository playerRepository) {
        this.repo = repo;
        this.playerRepository = playerRepository;
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<?> getMatchData(@PathVariable String matchId) {
        return repo.findMatchData(matchId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found")));
    }

    @GetMapping("/{matchId}/players")
    public ResponseEntity<?> playersInMatch(@PathVariable String matchId) {
        return repo.findMatchData(matchId)
                .<ResponseEntity<?>>map(md -> {
                    List<String> puuids = md.getMetadata() != null ? md.getMetadata().getParticipants() : List.of();
                    List<PlayerDoc> players = puuids.stream()
                            .map(playerRepository::findByPuuid)
                            .flatMap(Optional::stream)
                            .toList();
                    return ResponseEntity.ok(players);
                })
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found")));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> deleteEverywhere(@PathVariable String matchId) {
        boolean deleted = repo.deleteEverywhere(matchId);
        if (!deleted) {
            return ResponseEntity.status(404).body(Map.of("error","Not found"));
        }
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/ids")
    public ResponseEntity<List<MatchIdDoc>> listMatchIds(
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Rank rank
    ) {
        return ResponseEntity.ok(repo.listMatchIds(tier, rank));
    }

    @GetMapping("/ids/{matchId}")
    public ResponseEntity<?> getMatchId(@PathVariable String matchId) {
        return repo.findMatchId(matchId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error","Not found")));
    }

    @GetMapping("/ids/stats/count")
    public ResponseEntity<Map<String, Long>> countMatchIds(
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Rank rank
    ) {
        return ResponseEntity.ok(Map.of("count", repo.countMatchIds(tier, rank)));
    }

    @GetMapping("/ids/stats/distinct-tiers")
    public ResponseEntity<List<String>> distinctTiers() {
        return ResponseEntity.ok(repo.distinctTiers());
    }

    @GetMapping("/ids/stats/distinct-ranks")
    public ResponseEntity<List<String>> distinctRanks() {
        return ResponseEntity.ok(repo.distinctRanks());
    }

    @GetMapping("/participants/{puuid}")
    public ResponseEntity<List<MatchDataDoc>> getMatchesByPuuid(@PathVariable String puuid) {
        return ResponseEntity.ok(repo.findMatchesByPuuid(puuid));
    }

    @GetMapping("/participants/{puuid}/count")
    public ResponseEntity<Integer> countMatchesByPuuid(@PathVariable String puuid) {
        return ResponseEntity.ok((int) repo.countMatchesByPuuid(puuid));
    }

    @GetMapping("/stats/durations")
    public ResponseEntity<Map<String, Object>> durations(
            @RequestParam(required = false) Integer queueId,
            @RequestParam(required = false) String platformId,
            @RequestParam(required = false) Long startTimeFrom,
            @RequestParam(required = false) Long startTimeTo) {

        List<DurationStatsView> docs = repo.durationsStats(queueId, platformId, startTimeFrom, startTimeTo);
        Map<String,Object> res = new LinkedHashMap<>();
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
        var views = repo.championFrequency(limit);
        var res = views.stream().map(v -> {
            var m = new LinkedHashMap<String, Object>();
            m.put("championId", v.getChampionId());
            m.put("championName", v.getChampionName());
            m.put("count", v.getCount());
            return m;
        }).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/winrate-by-champion")
    public ResponseEntity<List<LinkedHashMap<String, Object>>> winrateByChampion(
            @RequestParam(required = false) Integer queueId,
            @RequestParam(required = false) String platformId) {
        var views = repo.winrateByChampion(queueId, platformId);
        var res = views.stream().map(v -> {
            var m = new LinkedHashMap<String,Object>();
            m.put("championId", v.getChampionId());
            m.put("championName", v.getChampionName());
            m.put("games", v.getGames());
            m.put("wins", v.getWins());
            m.put("winrate", v.getWinrate());
            return m;
        }).toList();
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/players/{puuid}")
    public ResponseEntity<PlayerRolesView> getPlayerWithRolesStatistics(@PathVariable String puuid) {
        var result = repo.getPlayerWithRolesStatistics(puuid);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }

    @GetMapping("/stats/champions/{champion}")
    public ResponseEntity<ChampionStatisticsView> getChampionStatistics(@PathVariable String champion) {
        var result = repo.getChampionStatistics(champion);
        if (result == null) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(result);
    }
}
