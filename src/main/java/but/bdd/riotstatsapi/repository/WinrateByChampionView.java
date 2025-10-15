package but.bdd.riotstatsapi.repository;
public interface WinrateByChampionView {
    Integer getChampionId();
    Integer getGames();
    Integer getWins();
    Double getWinrate();
}
