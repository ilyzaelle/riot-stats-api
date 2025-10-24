package but.bdd.riotstatsapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.List;
import java.util.Map;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "match_data")
@JsonInclude(Include.NON_NULL)
public class MatchDataDoc {
    @Id
    @JsonIgnore
    private ObjectId id;

    @Indexed(unique = true)
    @Field("metadata.matchId")
    private String matchId;

    private Metadata metadata;
    private Info info;

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Metadata {
        private String dataVersion;
        private String matchId;
        private List<String> participants;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Info {
        private Integer queueId;
        private String platformId;
        private Integer gameDuration;
        private Long gameStartTimestamp;
        private Long gameEndTimestamp;
        private String gameVersion;
        private List<Participant> participants;
        private List<Team> teams;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Participant {
        private String puuid;
        private Integer participantId;
        private Integer teamId;
        private Integer championId;
        private String championName;
        private String individualPosition;
        private String teamPosition;
        private Integer kills;
        private Integer deaths;
        private Integer assists;
        private Boolean win;
        private Integer goldEarned;
        private Integer totalDamageDealtToChampions;
        private Integer totalMinionsKilled;
        private String summonerName;
        private String summonerId;
        private String riotIdGameName;
        private String riotIdTagline;
        private Integer itemsPurchased;
        private Integer item0, item1, item2, item3, item4, item5, item6;
        private Map<String, Object> perks;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Team {
        private Integer teamId;
        private Boolean win;
        private List<Map<String,Object>> bans;
        private Map<String, Objective> objectives;
        private Map<String, Object> feats;
    }

    @Data @NoArgsConstructor @AllArgsConstructor @Builder
    public static class Objective {
        private Boolean first;
        private Integer kills;
    }
}
