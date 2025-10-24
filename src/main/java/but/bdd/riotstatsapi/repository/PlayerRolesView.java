package but.bdd.riotstatsapi.repository;

import java.util.List;

public interface PlayerRolesView {
    String getPuuid();
    String getRiotName();
    Integer getTotalGames();
    Integer getTotalWins();
    Double getWinrate();
    List<RoleView.PlayerRoleView> getRoles();
}
