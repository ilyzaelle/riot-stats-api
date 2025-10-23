package but.bdd.riotstatsapi.web;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.player.PlayerDoc;
import but.bdd.riotstatsapi.repository.PlayerRepository;
import but.bdd.riotstatsapi.repository.MatchDataRepository;
import but.bdd.riotstatsapi.domain.matchdata.MatchDataDoc;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/players")
@Tag(name = "players")
public class PlayerController {

    private final PlayerRepository playerRepository;
    private final MatchDataRepository matchDataRepository;

    public PlayerController(PlayerRepository playerRepository, MatchDataRepository matchDataRepository) {
        this.playerRepository = playerRepository;
        this.matchDataRepository = matchDataRepository;
    }

    @GetMapping
    public ResponseEntity<List<PlayerDoc>> list(
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Rank rank,
            @RequestParam(required = false) Integer minLp,
            @RequestParam(required = false) Integer maxLp,
            @RequestParam(required = false) Boolean veteran,
            @RequestParam(required = false) Boolean inactive,
            @RequestParam(required = false) Boolean freshBlood,
            @RequestParam(required = false, defaultValue = "leaguePoints,desc") String sort
    ) {
        List<PlayerDoc> p = playerRepository.search(tier, rank, minLp, maxLp, veteran, inactive, freshBlood);
        return ResponseEntity.ok(p);
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody PlayerDoc body) {
        if (playerRepository.findByPuuid(body.getPuuid()).isPresent()) return ResponseEntity.status(409).body(Map.of("error","Already exists"));
        return ResponseEntity.status(201).body(playerRepository.save(body));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Integer>> bulkUpsert(@RequestBody List<PlayerDoc> docs) {
        int inserted=0, updated=0;
        for (PlayerDoc d : docs) {
            Optional<PlayerDoc> existing = playerRepository.findByPuuid(d.getPuuid());
            if (existing.isPresent()) {
                playerRepository.save(d);
                updated++;
            } else {
                playerRepository.save(d);
                inserted++;
            }
        }
        return ResponseEntity.ok(Map.of("inserted", inserted, "updated", updated, "total", inserted+updated));
    }

    @GetMapping("/{puuid}")
    public Optional<PlayerDoc> getByPuuid(@PathVariable String puuid) {
        return playerRepository.findByPuuid(puuid);
                /*.<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Not found")));*/
    }

    @PutMapping("/{puuid}")
    public ResponseEntity<?> put(@PathVariable String puuid, @RequestBody PlayerDoc body) {
        Optional<PlayerDoc> existing = playerRepository.findByPuuid(puuid);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        body.setPuuid(puuid);
        return ResponseEntity.ok(playerRepository.save(body));
    }

    @PatchMapping("/{puuid}")
    public ResponseEntity<?> patch(@PathVariable String puuid, @RequestBody Map<String, Object> patch) {
        Optional<PlayerDoc> existing = playerRepository.findByPuuid(puuid);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        PlayerDoc e = existing.get();
        if (patch.containsKey("tier")) e.setTier(Tier.valueOf(String.valueOf(patch.get("tier"))));
        if (patch.containsKey("rank")) e.setRank(Rank.valueOf(String.valueOf(patch.get("rank"))));
        if (patch.containsKey("leaguePoints")) e.setLeaguePoints((Integer) patch.get("leaguePoints"));
        if (patch.containsKey("wins")) e.setWins((Integer) patch.get("wins"));
        if (patch.containsKey("losses")) e.setLosses((Integer) patch.get("losses"));
        if (patch.containsKey("veteran")) e.setVeteran((Boolean) patch.get("veteran"));
        if (patch.containsKey("inactive")) e.setInactive((Boolean) patch.get("inactive"));
        if (patch.containsKey("freshBlood")) e.setFreshBlood((Boolean) patch.get("freshBlood"));
        return ResponseEntity.ok(playerRepository.save(e));
    }

    @DeleteMapping("/{puuid}")
    public ResponseEntity<?> delete(@PathVariable String puuid) {
        Optional<PlayerDoc> existing = playerRepository.findByPuuid(puuid);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        playerRepository.delete(existing.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Object>> count(@RequestParam(required = false) Tier tier,
                                                   @RequestParam(required = false) Rank rank,
                                                   @RequestParam(required = false) Integer minLp,
                                                   @RequestParam(required = false) Integer maxLp) {
        List<PlayerDoc> p = playerRepository.search(tier, rank, minLp, maxLp, null, null, null);
        Map<String,Object> res = new java.util.LinkedHashMap<>();
        res.put("count", p.size());
        return ResponseEntity.ok(res);
    }

    @GetMapping("/stats/leaderboard")
    public ResponseEntity<List<PlayerDoc>> leaderboard(@RequestParam(defaultValue = "leaguePoints") String field,
                                                       @RequestParam(defaultValue = "100") Integer limit) {
        List<PlayerDoc> list = playerRepository.leaderboard(field, limit);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/stats/winrate/{puuid}")
    public ResponseEntity<Map<String, Object>> winrate(@PathVariable String puuid) {
        return playerRepository.findByPuuid(puuid)
                .map(pl -> {
                    int wins = pl.getWins();
                    int losses = pl.getLosses();
                    double wr = (wins + losses) == 0 ? 0.0 : (wins * 100.0) / (wins + losses);
                    Map<String, Object> res = new java.util.LinkedHashMap<>();
                    res.put("puuid", puuid);
                    res.put("wins", wins);
                    res.put("losses", losses);
                    res.put("winrate", wr);
                    return ResponseEntity.ok(res);
                })
                .orElseGet(() -> {
                    Map<String, Object> err = new java.util.LinkedHashMap<>();
                    err.put("error", "Not found");
                    return ResponseEntity.status(404).body(err);
                });
    }

    @GetMapping("/{puuid}/matches")
    public ResponseEntity<List<MatchDataDoc>> matches(@PathVariable String puuid) {
        List<MatchDataDoc> list = matchDataRepository.findAllByParticipantPuuid(puuid);
        return ResponseEntity.ok(list); // sort "info.gameEndTimestamp,desc"
    }
}
