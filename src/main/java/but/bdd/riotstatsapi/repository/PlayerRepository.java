package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.player.PlayerDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface PlayerRepository extends MongoRepository<PlayerDoc, String> {
    @Query("{ 'puuid': ?0 }")
    Optional<PlayerDoc> findByPuuid(String puuid);

    @Query("""
{
      $expr: {
        $and: [
          { $or: [ { $eq: [?0, null] }, { $eq: ['$tier', ?0] } ] },
          { $or: [ { $eq: [?1, null] }, { $eq: ['$rank', ?1] } ] },
          { $or: [ { $eq: [?2, null] }, { $gte: ['$leaguePoints', ?2] } ] },
          { $or: [ { $eq: [?3, null] }, { $lte: ['$leaguePoints', ?3] } ] },
          { $or: [ { $eq: [?4, null] }, { $eq: ['$veteran', ?4] } ] },
          { $or: [ { $eq: [?5, null] }, { $eq: ['$inactive', ?5] } ] },
          { $or: [ { $eq: [?6, null] }, { $eq: ['$freshBlood', ?6] } ] }
        ]
      }
    }"""
    )
    List<PlayerDoc> search(Tier tier, Rank rank, Integer minLp, Integer maxLp,
                           Boolean veteran, Boolean inactive, Boolean freshBlood);

    @Aggregation(pipeline = {
            "{$sort: {?0: -1}}",
            "{$limit: ?1}"
    })
    List<PlayerDoc> leaderboard(String field, Integer size);


}
