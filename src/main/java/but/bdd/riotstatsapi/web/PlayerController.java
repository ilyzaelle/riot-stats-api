package but.bdd.riotstatsapi.web;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.PlayerDoc;
import but.bdd.riotstatsapi.repository.PlayerRepository;
import but.bdd.riotstatsapi.repository.MatchDataRepository;
import but.bdd.riotstatsapi.domain.MatchDataDoc;
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
            @RequestParam(required = false) Boolean freshBlood
    ) {
        List<PlayerDoc> p = playerRepository.search(tier, rank, minLp, maxLp, veteran, inactive, freshBlood);
        return ResponseEntity.ok(p);
    }

    @GetMapping("/{puuid}")
    public Optional<PlayerDoc> getByPuuid(@PathVariable String puuid) {
        return playerRepository.findByPuuid(puuid);
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
        return ResponseEntity.ok(list);
    }
}
