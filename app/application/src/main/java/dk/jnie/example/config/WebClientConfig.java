package dk.jnie.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {
    @Bean
    public WebClient adviceWebClient(WebClient.Builder builder,
                                     @Value("${mma.outbound.advice-slip-api.url}") String url) {
        return builder
                .baseUrl(url)
                .build();
    }
}
