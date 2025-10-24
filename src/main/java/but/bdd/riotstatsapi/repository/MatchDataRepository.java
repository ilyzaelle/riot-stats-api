package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.matchdata.MatchDataDoc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.util.List;
import java.util.Optional;

public interface MatchDataRepository extends MongoRepository<MatchDataDoc, String> {

    @Query("{ 'metadata.matchId': ?0 }")
    Optional<MatchDataDoc> findByMetadataMatchId(String matchId);

    @Query("{ 'info.participants': { $elemMatch: { 'puuid': ?0 } } }")
    List<MatchDataDoc> findAllByParticipantPuuid(String puuid);

    @Aggregation(pipeline = {
        "{ $match: { $expr: { $and: [" +
        "  { $or: [ { $eq: [?0, null] }, { $eq: ['$info.queueId', ?0] } ] }," +
        "  { $or: [ { $eq: [?1, null] }, { $eq: ['$info.platformId', ?1] } ] }," +
        "  { $or: [ { $eq: [?2, null] }, { $gte: ['$info.gameStartTimestamp', ?2] } ] }," +
        "  { $or: [ { $eq: [?3, null] }, { $lte: ['$info.gameStartTimestamp', ?3] } ] }" +
        "] } } }",
        "{ $group: { _id: 0, min: { $min: '$info.gameDuration' }, max: { $max: '$info.gameDuration' }, avg: { $avg: '$info.gameDuration' } } }",
        "{ $project: { _id: 0, min: 1, max: 1, avg: 1 } }"
    })
    List<DurationStatsView> durationsStats(Integer queueId, String platformId, Long startTimeFrom, Long startTimeTo);

    @Aggregation(pipeline = {
        "{ $unwind: '$info.participants' }",
        "{ $group: { _id: '$info.participants.championId', count: { $sum: 1 }, championName: { $first: '$info.participants.championName' } } }",
        "{ $sort: { count: -1 } }",
        "{ $limit: ?0 }",
        "{ $project: { _id: 0, championId: '$_id', championName: 1, count: 1 } }"
    })
    List<ChampionCountView> championFrequency(int limit);

    @Aggregation(pipeline = {
        "{ $match: { $expr: { $and: [" +
        "  { $or: [ { $eq: [?0, null] }, { $eq: ['$info.queueId', ?0] } ] }," +
        "  { $or: [ { $eq: [?1, null] }, { $eq: ['$info.platformId', ?1] } ] }" +
        "] } } }",
        "{ $unwind: '$info.participants' }",
        "{ $group: { _id: '$info.participants.championId', " +
        "games: { $sum: 1 }, " +
        "wins: { $sum: { $cond: [ '$info.participants.win', 1, 0 ] } }, " +
        "championName: { $first: '$info.participants.championName' } } }",
        "{ $project: { _id: 0, championId: '$_id', championName: 1, games: 1, wins: 1, " +
        "winrate: { $multiply: [ { $cond: [ { $eq: ['$games', 0] }, 0, { $divide: ['$wins', '$games'] } ] }, 100 ] } } }",
        "{ $sort: { games: -1 } }"
    })
    List<WinrateByChampionView> winrateByChampion(Integer queueId, String platformId);
}
