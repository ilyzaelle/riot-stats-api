package but.bdd.riotstatsapi.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
@Document(collection = "match_ids")
public class MatchIdDoc {
    @Id
    @JsonIgnore
    private ObjectId id;

    @Indexed(unique = true)
    @Field("matchId")
    private String matchId;

    private Tier tier;
    private Rank rank;
}
