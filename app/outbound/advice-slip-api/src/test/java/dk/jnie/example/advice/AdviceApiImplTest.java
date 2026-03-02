package dk.jnie.example.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.jnie.example.advice.mappers.AdviceObjectMapper;
import dk.jnie.example.advice.model.AdviceResponse;
import dk.jnie.example.model.MultiAggregate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for AdviceApiImpl.
 * Tests the external API integration layer that calls the Advice Slip API.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdviceApiImpl Tests")
class AdviceApiImplTest {

    @Mock
    private WebClient adviceWebClient;

    @Mock
    private WebClient.RequestHeadersUriSpec requestHeadersUriSpec;

    @Mock
    private WebClient.ResponseSpec responseSpec;

    private ObjectMapper objectMapper;
    private AdviceObjectMapper adviceObjectMapper;
    private AdviceApiImpl adviceApiImpl;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        adviceObjectMapper = new AdviceObjectMapperImpl();
        adviceApiImpl = new AdviceApiImpl(objectMapper, adviceObjectMapper, adviceWebClient);
    }

    @Test
    @DisplayName("getRandomAdvice returns valid advice from API response")
    void getRandomAdvice_ReturnsValidAdvice() throws Exception {
        // Arrange
        String jsonResponse = "{\"slip\":{\"id\":125,\"advice\":\"Don't be afraid to ask questions.\"}}";
        
        WebClient.RequestHeadersUriSpec<?> requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.UriSpec<?> uriSpec = org.mockito.Mockito.mock(WebClient.UriSpec.class);
        
        when(adviceWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.uri("/advice")).thenReturn((WebClient.RequestHeadersUriSpec<?>) uriSpec);
        when(uriSpec.accept(MediaType.TEXT_HTML)).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(jsonResponse));

        // Act
        Mono<MultiAggregate> result = adviceApiImpl.getRandomAdvice();

        // Assert
        StepVerifier.create(result)
                .assertNext(aggregate -> {
                    assertThat(aggregate.getAnswer()).isEqualTo("Don't be afraid to ask questions.");
                })
                .verifyComplete();
    }

    @Test
    @DisplayName("getRandomAdvice handles malformed JSON response")
    void getRandomAdvice_HandlesMalformedJsonResponse() {
        // Arrange
        String malformedJson = "{invalid json}";
        
        WebClient.RequestHeadersUriSpec<?> requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.UriSpec<?> uriSpec = org.mockito.Mockito.mock(WebClient.UriSpec.class);
        
        when(adviceWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.uri("/advice")).thenReturn((WebClient.RequestHeadersUriSpec<?>) uriSpec);
        when(uriSpec.accept(MediaType.TEXT_HTML)).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.just(malformedJson));

        // Act
        Mono<MultiAggregate> result = adviceApiImpl.getRandomAdvice();

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    @DisplayName("getRandomAdvice handles empty response")
    void getRandomAdvice_HandlesEmptyResponse() {
        // Arrange
        WebClient.RequestHeadersUriSpec<?> requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.UriSpec<?> uriSpec = org.mockito.Mockito.mock(WebClient.UriSpec.class);
        
        when(adviceWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.uri("/advice")).thenReturn((WebClient.RequestHeadersUriSpec<?>) uriSpec);
        when(uriSpec.accept(MediaType.TEXT_HTML)).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.empty());

        // Act
        Mono<MultiAggregate> result = adviceApiImpl.getRandomAdvice();

        // Assert
        StepVerifier.create(result)
                .verifyComplete();
    }

    @Test
    @DisplayName("getRandomAdvice propagates WebClient errors")
    void getRandomAdvice_PropagatesWebClientErrors() {
        // Arrange
        WebClient.RequestHeadersUriSpec<?> requestSpec = org.mockito.Mockito.mock(WebClient.RequestHeadersUriSpec.class);
        WebClient.UriSpec<?> uriSpec = org.mockito.Mockito.mock(WebClient.UriSpec.class);
        
        when(adviceWebClient.get()).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.uri("/advice")).thenReturn((WebClient.RequestHeadersUriSpec<?>) uriSpec);
        when(uriSpec.accept(MediaType.TEXT_HTML)).thenReturn((WebClient.RequestHeadersUriSpec<?>) requestSpec);
        when(requestSpec.retrieve()).thenReturn(responseSpec);
        when(responseSpec.bodyToMono(String.class)).thenReturn(Mono.error(new RuntimeException("Connection refused")));

        // Act
        Mono<MultiAggregate> result = adviceApiImpl.getRandomAdvice();

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();
    }
}
