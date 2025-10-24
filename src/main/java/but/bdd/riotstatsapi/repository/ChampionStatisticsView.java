package but.bdd.riotstatsapi.repository;

import java.util.List;

public interface ChampionStatisticsView {
    List<RoleView> getRoles();
    String getChampion();
    Integer getPicks();
}
