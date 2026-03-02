package dk.jnie.example.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import dk.jnie.example.advice.mappers.AdviceObjectMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.codec.json.Jackson2JsonDecoder;
import org.springframework.http.codec.json.Jackson2JsonEncoder;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Test configuration for AdviceApiImpl integration tests.
 *
 * <p>Provides:
 * <ul>
 *   <li>ObjectMapper with snake_case strategy</li>
 *   <li>WebClient configured with WireMock base URL from property</li>
 *   <li>AdviceObjectMapper for domain mapping</li>
 *   <li>AdviceApiImpl as a service bean</li>
 * </ul>
 *
 * @see AdviceApiImplTest
 */
@TestConfiguration
@EnableAutoConfiguration
@ComponentScan(basePackages = "dk.jnie.example.advice.mappers")
public class AdviceTestConfig {

    /**
     * ObjectMapper configured with snake_case naming strategy to match
     * the external API's JSON format.
     */
    @Bean
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        return mapper;
    }

    /**
     * WebClient configured to point to the WireMock server.
     *
     * <p>The base URL is injected from the 'advice.api.base-url' property,
     * which is set dynamically by AdviceApiImplTest via @DynamicPropertySource.
     * This allows each test to use its own WireMock port.</p>
     *
     * @param baseUrl the WireMock server URL (injected from properties)
     * @return WebClient configured for the WireMock backend
     */
    @Bean
    public WebClient adviceWebClient(
            @Value("${advice.api.base-url:http://localhost:8080}") String baseUrl,
            ObjectMapper objectMapper) {

        return WebClient.builder()
                .baseUrl(baseUrl)
                .codecs(configurer -> {
                    configurer.defaultCodecs().jackson2JsonEncoder(
                            new Jackson2JsonEncoder(objectMapper));
                    configurer.defaultCodecs().jackson2JsonDecoder(
                            new Jackson2JsonDecoder(objectMapper));
                })
                .build();
    }

    /**
     * AdviceApiImpl service bean that will be injected into tests.
     */
    @Bean
    public AdviceApiImpl adviceApiImpl(ObjectMapper objectMapper,
                                        AdviceObjectMapper mapper,
                                        WebClient adviceWebClient) {
        return new AdviceApiImpl(objectMapper, mapper, adviceWebClient);
    }

    /**
     * Test controller that exposes AdviceApiImpl via HTTP GET endpoint.
     * This allows WebTestClient to make real HTTP calls to the service.
     */
    @Bean
    public AdviceTestController adviceTestController(AdviceApiImpl adviceApi) {
        return new AdviceTestController(adviceApi);
    }
}
