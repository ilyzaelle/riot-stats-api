package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.MatchDataDoc;
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

    @Aggregation(pipeline = {
            """
   {
     $match: {
       "info.participants.puuid": {
         $eq: "?0"
       }
     }
   }""",
            """
   {
     $unwind: "$info.participants"
   }""",
            """
   {
     $set: {
       puuid: "$info.participants.puuid",
       riotName: {
         $concat: [
           "$info.participants.riotIdGameName",
           "#",
           "$info.participants.riotIdTagline"
         ]
       },
       champion: "$info.participants.championName",
       role: "$info.participants.individualPosition",
       winFlag: "$info.participants.win"
     }
   }""",
            """
   {
     $match: {
       puuid: {
         $type: "string",
         $ne: ""
       },
       champion: {
         $type: "string",
         $ne: ""
       },
       role: {
         $type: "string",
         $nin: ["", "NONE", "INVALID"]
       },
       riotName: {
         $regex: ".+#.+"
       }
     }
   }""",
            """
   {
     $group: {
       _id: {
         puuid: "$puuid",
         riotName: "$riotName",
         role: "$role",
         champion: "$champion"
       },
       games: {
         $sum: 1
       },
       wins: {
         $sum: {
           $cond: ["$winFlag", 1, 0]
         }
       }
     }
   }""",
            """
   {
     $set: {
       puuid: "$_id.puuid",
       riotName: "$_id.riotName",
       role: "$_id.role",
       champion: "$_id.champion"
     }
   }""",
            """
   {
     $unset: "_id"
   }""",
            """
   {
     $addFields: {
       winrate: {
         $multiply: [
           {
             $divide: ["$wins", "$games"]
           },
           100
         ]
       }
     }
   }""",
            """
   {
     $sort: {
       puuid: 1,
       role: 1,
       games: -1,
       wins: -1,
       champion: 1
     }
   }""",
            """
   {
     $group: {
       _id: {
         puuid: "$puuid",
         riotName: "$riotName",
         role: "$role"
       },
       totalRoleGames: {
         $sum: "$games"
       },
       totalRoleWins: {
         $sum: "$wins"
       },
       favChampion: {
         $first: "$champion"
       },
       favChampionGames: {
         $first: "$games"
       },
       favChampionWins: {
         $first: "$wins"
       },
       favChampionWinrate: {
         $first: {
           $round: ["$winrate", 2]
         }
       }
     }
   }""",
            """
   {
     $sort: {
       "_id.puuid": 1,
       totalRoleGames: -1
     }
   }""",
            """
   {
     $group: {
       _id: {
         puuid: "$_id.puuid",
         riotName: "$_id.riotName"
       },
       roles: {
         $push: {
           role: "$_id.role",
           games: "$totalRoleGames",
           wins: "$totalRoleWins",
           winrate: {
             $round: [
               {
                 $multiply: [
                   {
                     $divide: [
                       "$totalRoleWins",
                       "$totalRoleGames"
                     ]
                   },
                   100
                 ]
               },
               2
             ]
           },
           favoriteChampion: {
             name: "$favChampion",
             games: "$favChampionGames",
             wins: "$favChampionWins",
             winrate: "$favChampionWinrate"
           }
         }
       },
       totalGames: {
         $sum: "$totalRoleGames"
       },
       totalWins: {
         $sum: "$totalRoleWins"
       }
     }
   }""",
            """
   {
     $addFields: {
       winrate: {
         $round: [
           {
             $multiply: [
               {
                 $divide: [
                   "$totalWins",
                   "$totalGames"
                 ]
               },
               100
             ]
           },
           2
         ]
       }
     }
   }""",
            """
   {
     $match: {
       "_id.puuid": {
         $eq: "?0"
       }
     }
   }""",
            """
   {
     $project: {
       _id: 0,
       puuid: "$_id.puuid",
       riotName: "$_id.riotName",
       totalGames: 1,
       totalWins: 1,
       winrate: 1,
       roles: 1
     }
   }
"""
    })
    PlayerRolesView getPlayerWithRolesStatistics(String puuid);

    @Aggregation(pipeline = {
            """
           {
                $match: {
                    "info.participants.championName": {
                        $eq: ?0
                    }
                }
            }""",
            """
            { $unwind: "$info.participants" }
            """,
            """
            { $set: {
                champion: "$info.participants.championName",
                role: "$info.participants.individualPosition",
                winFlag: "$info.participants.win"
            } }
            """,
            """
            { $match: {
                champion: { $type: "string", $ne: "" },
                role: { $type: "string", $nin: ["", "NONE", "INVALID"] }
            } }
            """,
            """
            { $group: {
                _id: { champion: "$champion", role: "$role" },
                games: { $sum: 1 },
                wins: { $sum: { $cond: ["$winFlag", 1, 0] } }
            } }
            """,
            """
            { $addFields: {
                winrate: { $multiply: [ { $divide: ["$wins", "$games"] }, 100 ] }
            } }
            """,
            """
            { $sort: { "_id.champion": 1, games: -1 } }
            """,
            """
            { $group: {
                _id: "$_id.champion",
                roles: {
                    $push: {
                        role: "$_id.role",
                        games: "$games",
                        wins: "$wins",
                        winrate: { $round: ["$winrate", 2] }
                    }
                },
                totalGames: { $sum: "$games" }
            } }
            """,
            """
            { $addFields: { picks: "$totalGames" } }
            """,
            """
            { $project: {
                _id: 0,
                champion: "$_id",
                roles: 1,
                picks: 1
            } }
            """,
            """
           {
                $match: {
                    "champion": {
                        $eq: ?0
                    }
                }
            }""",
            """
            { $sort: { picks: -1, champion: 1 } }
            """
    })
    ChampionStatisticsView getChampionStatistics(String champion);
}
