package but.bdd.riotstatsapi.repository;

public interface RoleView {
    String getRole();
    Integer getGames();
    Integer getWins();
    Double getWinrate();

    interface PlayerRoleView {
        String getRole();
        Integer getGames();
        Integer getWins();
        Double getWinrate();
        ChampionView getFavoriteChampion();
    }
}