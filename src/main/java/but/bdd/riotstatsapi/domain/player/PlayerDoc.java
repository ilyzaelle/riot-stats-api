package but.bdd.riotstatsapi.domain.player;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document("players")
public class PlayerDoc {
    // Identifiant technique MongoDB — caché dans les réponses JSON
    @Id
    @JsonIgnore
    private ObjectId id;

    // Clé métier : unique + indexée
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