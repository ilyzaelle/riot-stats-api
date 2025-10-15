package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.matchid.MatchIdDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchIdRepository extends MongoRepository<MatchIdDoc, String> {
    @Query("{ 'matchId': ?0 }")
    Optional<MatchIdDoc> findByMatchId(String matchId);

    @Query("{ $and: [ { 'tier': { $eq: ?0 } }, { 'rank': { $eq: ?1 } } ] }")
    Page<MatchIdDoc> findAllByTierAndRank(Tier tier, Rank rank, Pageable pageable);

    @Query("{ }")
    Page<MatchIdDoc> findAllPaged(Pageable pageable);

    @Aggregation(pipeline = {
      "{ $group: { _id: '$tier' } }",
      "{ $replaceWith: '$_id' }"
    })
    List<String> distinctTiers();

    @Query("{ $and: [ "
        + "{ $or: [ { 'tier': { $eq: ?0 } }, { ?0: { $eq: null } } ] }, "
        + "{ $or: [ { 'rank': { $eq: ?1 } }, { ?1: { $eq: null } } ] } "
        + "] }")
    long countByOptionalFilters(Tier tier, Rank rank);
}
