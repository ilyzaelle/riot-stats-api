package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.MatchIdDoc;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchIdRepository extends MongoRepository<MatchIdDoc, String> {
    @Query("{ 'matchId': ?0 }")
    Optional<MatchIdDoc> findByMatchId(String matchId);

    @Query("{ $and: [ { 'tier': { $eq: ?0 } }, { 'rank': { $eq: ?1 } } ] }")
    List<MatchIdDoc> findAllByTierAndRank(Tier tier, Rank rank);

    @Query("{ }")
    List<MatchIdDoc> findAll();

    @Aggregation(pipeline = {
      "{ $group: { _id: '$tier' } }",
      "{ $project: { _id: 0, tier: '$_id' } }"
    })
    List<String> distinctTiers();

    @Aggregation(pipeline = {
        "{ $group: { _id: '$rank' } }",
        "{ $project: { _id: 0, rank: '$_id' } }"
    })
    List<String> distinctRanks();

    @Query("{ $and: [ "
        + "{ $or: [ { 'tier': { $eq: ?0 } }, { ?0: { $eq: null } } ] }, "
        + "{ $or: [ { 'rank': { $eq: ?1 } }, { ?1: { $eq: null } } ] } "
        + "] }")
    long countByOptionalFilters(Tier tier, Rank rank);
}
