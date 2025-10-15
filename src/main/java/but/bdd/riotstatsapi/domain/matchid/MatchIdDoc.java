package but.bdd.riotstatsapi.domain.matchid;

import but.bdd.riotstatsapi.domain.Rank;
import but.bdd.riotstatsapi.domain.Tier;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "match_ids")
public class MatchIdDoc {
    @Transient
    @JsonIgnore
    private ObjectId _id;

    @Id
    @Field("matchId")
    private String matchId;

    private Tier tier;
    private Rank rank;
}
