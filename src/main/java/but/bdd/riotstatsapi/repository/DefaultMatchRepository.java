package but.bdd.riotstatsapi.repository;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import but.bdd.riotstatsapi.domain.MatchDataDoc;
import but.bdd.riotstatsapi.domain.MatchIdDoc;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DefaultMatchRepository implements MatchRepository {

    private final MatchIdRepository matchIdRepository;
    private final MatchDataRepository matchDataRepository;

    public DefaultMatchRepository(MatchIdRepository matchIdRepository,
                                         MatchDataRepository matchDataRepository) {
        this.matchIdRepository = matchIdRepository;
        this.matchDataRepository = matchDataRepository;
    }

    @Override
    public List<MatchIdDoc> listMatchIds(Tier tier, Rank rank) {
        if (tier != null && rank != null) {
            return matchIdRepository.findAllByTierAndRank(tier, rank);
        }
        return matchIdRepository.findAll();
    }

    @Override
    public Optional<MatchIdDoc> findMatchId(String matchId) {
        return matchIdRepository.findByMatchId(matchId);
    }

    @Override
    public long countMatchIds(Tier tier, Rank rank) {
        return matchIdRepository.countByOptionalFilters(tier, rank);
    }

    @Override
    public List<String> distinctTiers() {
        return matchIdRepository.distinctTiers();
    }

    @Override
    public List<String> distinctRanks() {
        return matchIdRepository.distinctRanks();
    }

    @Override
    public Optional<MatchDataDoc> findMatchData(String matchId) {
        return matchDataRepository.findByMetadataMatchId(matchId);
    }

    @Override
    public List<MatchDataDoc> findMatchesByPuuid(String puuid) {
        return matchDataRepository.findAllByParticipantPuuid(puuid);
    }

    @Override
    public long countMatchesByPuuid(String puuid) {
        return matchDataRepository.findAllByParticipantPuuid(puuid).size();
    }

    @Override
    public boolean deleteEverywhere(String matchId) {
        boolean deleted = false;

        var mid = matchIdRepository.findByMatchId(matchId);
        if (mid.isPresent()) {
            matchIdRepository.delete(mid.get());
            deleted = true;
        }

        var mdata = matchDataRepository.findByMetadataMatchId(matchId);
        if (mdata.isPresent()) {
            matchDataRepository.delete(mdata.get());
            deleted = true;
        }

        return deleted;
    }

    @Override
    public List<DurationStatsView> durationsStats(Integer queueId, String platformId, Long startTimeFrom, Long startTimeTo) {
        return matchDataRepository.durationsStats(queueId, platformId, startTimeFrom, startTimeTo);
    }

    @Override
    public List<ChampionCountView> championFrequency(int limit) {
        return matchDataRepository.championFrequency(Math.max(1, Math.min(limit, 500)));
    }

    @Override
    public List<WinrateByChampionView> winrateByChampion(Integer queueId, String platformId) {
        return matchDataRepository.winrateByChampion(queueId, platformId);
    }

    @Override
    public PlayerRolesView getPlayerWithRolesStatistics(String puuid) {
        return matchDataRepository.getPlayerWithRolesStatistics(puuid);
    }

    @Override
    public ChampionStatisticsView getChampionStatistics(String champion) {
        return matchDataRepository.getChampionStatistics(champion);
    }
}
