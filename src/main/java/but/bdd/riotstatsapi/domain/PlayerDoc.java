package but.bdd.riotstatsapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("players")
public class PlayerDoc {
    @Id
    @JsonIgnore
    private ObjectId id;

    @Indexed(unique = true)
    @Field("puuid")
    private String puuid;

    private Tier tier;
    private Rank rank;
    private int leaguePoints;
    private int wins;
    private int losses;
    private boolean veteran;
    private boolean inactive;
    private boolean freshBlood;
}