package but.bdd.riotstatsapi.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI riotOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Riot Stats API")
                        .version("1.0.0")
                        .description("API REST pour trois collections MongoDB: match_ids, players, match_data."));
    }
}
