package but.bdd.riotstatsapi.repository;
public interface WinrateByChampionView {
    Integer getChampionId();
    String getChampionName();
    Integer getGames();
    Integer getWins();
    Double getWinrate();
}
