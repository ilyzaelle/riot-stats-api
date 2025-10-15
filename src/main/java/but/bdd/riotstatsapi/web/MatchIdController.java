package but.bdd.riotstatsapi.web;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.matchid.MatchIdDoc;
import but.bdd.riotstatsapi.repository.MatchIdRepository;
import but.bdd.riotstatsapi.repository.MatchDataRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/match-ids")
@Tag(name = "match-ids")
public class MatchIdController {

    private final MatchIdRepository matchIdRepository;
    private final MatchDataRepository matchDataRepository;

    public MatchIdController(MatchIdRepository matchIdRepository, MatchDataRepository matchDataRepository) {
        this.matchIdRepository = matchIdRepository;
        this.matchDataRepository = matchDataRepository;
    }

    @GetMapping
    public ResponseEntity<PageResponse<MatchIdDoc>> list(
            @RequestParam(required = false) Tier tier,
            @RequestParam(required = false) Rank rank,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size,
            @RequestParam(required = false, defaultValue = "matchId,asc") String sort
    ) {
        String[] s = sort.split(",");
        Sort sortObj = Sort.by(Sort.Direction.fromString(s.length>1?s[1]:"asc"), s[0]);
        Pageable pageable = PageRequest.of(page, size, sortObj);
        Page<MatchIdDoc> p;
        if (tier != null && rank != null) {
            p = matchIdRepository.findAllByTierAndRank(tier, rank, pageable);
        } else {
            p = matchIdRepository.findAllPaged(pageable);
        }
        return ResponseEntity.ok(new PageResponse<>(p.getContent(), p.getNumber(), p.getSize(), p.getTotalElements(), p.getTotalPages(), sort));
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody MatchIdDoc body) {
        Optional<MatchIdDoc> existing = matchIdRepository.findByMatchId(body.getMatchId());
        if (existing.isPresent()) return ResponseEntity.status(409).body(Map.of("error","Already exists"));
        return ResponseEntity.status(201).body(matchIdRepository.save(body));
    }

    @PostMapping("/bulk")
    public ResponseEntity<Map<String, Integer>> bulkUpsert(@RequestBody List<MatchIdDoc> docs) {
        int inserted=0, updated=0;
        for (MatchIdDoc d : docs) {
            Optional<MatchIdDoc> existing = matchIdRepository.findByMatchId(d.getMatchId());
            if (existing.isPresent()) {
                matchIdRepository.save(d);
                updated++;
            } else {
                matchIdRepository.save(d);
                inserted++;
            }
        }
        return ResponseEntity.ok(Map.of("inserted", inserted, "updated", updated, "total", inserted+updated));
    }

    @GetMapping("/{matchId}")
    public ResponseEntity<?> getByMatchId(@PathVariable String matchId) {
        return matchIdRepository.findByMatchId(matchId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }

    @PutMapping("/{matchId}")
    public ResponseEntity<?> put(@PathVariable String matchId, @RequestBody MatchIdDoc body) {
        Optional<MatchIdDoc> existing = matchIdRepository.findByMatchId(matchId);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        body.setMatchId(matchId);
        return ResponseEntity.ok(matchIdRepository.save(body));
    }

    @PatchMapping("/{matchId}")
    public ResponseEntity<?> patch(@PathVariable String matchId, @RequestBody Map<String, Object> patch) {
        Optional<MatchIdDoc> existing = matchIdRepository.findByMatchId(matchId);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        MatchIdDoc e = existing.get();
        if (patch.containsKey("tier")) e.setTier(Tier.valueOf(String.valueOf(patch.get("tier"))));
        if (patch.containsKey("rank")) e.setRank(Rank.valueOf(String.valueOf(patch.get("rank"))));
        return ResponseEntity.ok(matchIdRepository.save(e));
    }

    @DeleteMapping("/{matchId}")
    public ResponseEntity<?> delete(@PathVariable String matchId) {
        Optional<MatchIdDoc> existing = matchIdRepository.findByMatchId(matchId);
        if (existing.isEmpty()) return ResponseEntity.status(404).body(Map.of("error","Not found"));
        matchIdRepository.delete(existing.get());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/stats/count")
    public ResponseEntity<Map<String, Long>> count(@RequestParam(required = false) Tier tier,
                                                   @RequestParam(required = false) Rank rank) {
        long count = matchIdRepository.countByOptionalFilters(tier, rank);
        return ResponseEntity.ok(Map.of("count", count));
    }

    @GetMapping("/stats/distinct-tiers")
    public ResponseEntity<List<String>> distinctTiers() {
        return ResponseEntity.ok(matchIdRepository.distinctTiers());
    }

    @GetMapping("/{matchId}/data")
    public ResponseEntity<?> getMatchData(@PathVariable String matchId) {
        return matchDataRepository.findByMetadataMatchId(matchId)
                .<ResponseEntity<?>>map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(404).body(Map.of("error", "Not found")));
    }
}
