package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.MatchDataDoc;
import but.bdd.riotstatsapi.domain.MatchIdDoc;

import java.util.List;
import java.util.Optional;

public interface MatchRepository {

    List<MatchIdDoc> listMatchIds(Tier tier, Rank rank);
    Optional<MatchIdDoc> findMatchId(String matchId);
    long countMatchIds(Tier tier, Rank rank);
    List<String> distinctTiers();
    List<String> distinctRanks();

    Optional<MatchDataDoc> findMatchData(String matchId);
    List<MatchDataDoc> findMatchesByPuuid(String puuid);
    long countMatchesByPuuid(String puuid);

    boolean deleteEverywhere(String matchId);

    List<DurationStatsView> durationsStats(Integer queueId, String platformId, Long startTimeFrom, Long startTimeTo);
    List<ChampionCountView> championFrequency(int limit);
    List<WinrateByChampionView> winrateByChampion(Integer queueId, String platformId);
    PlayerRolesView getPlayerWithRolesStatistics(String puuid);
    ChampionStatisticsView getChampionStatistics(String champion);

}
