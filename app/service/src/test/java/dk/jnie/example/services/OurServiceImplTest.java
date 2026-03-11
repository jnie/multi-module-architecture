package dk.jnie.example.services;

import dk.jnie.example.domain.model.DomainRequest;
import dk.jnie.example.domain.model.DomainResponse;
import dk.jnie.example.domain.model.MultiAggregate;
import dk.jnie.example.domain.outbound.AdviceApi;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for OurServiceImpl.
 * Tests the business logic layer that orchestrates calls to the AdviceApi.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("OurServiceImpl Tests")
class OurServiceImplTest {

    @Mock
    private AdviceApi adviceAPI;

    private OurServiceImpl ourService;

    @BeforeEach
    void setUp() {
        ourService = new OurServiceImpl(adviceAPI);
    }

    @Test
    @DisplayName("getAnAdvice returns advice successfully")
    void getAnAdvice_ReturnsAdviceSuccessfully() {
        // Arrange
        String expectedAdvice = "Don't be afraid to ask questions.";
        DomainRequest request = DomainRequest.builder()
                .question("anything")
                .build();
        MultiAggregate mockAggregate = MultiAggregate.builder()
                .answer(expectedAdvice)
                .build();

        when(adviceAPI.getRandomAdvice()).thenReturn(Mono.just(mockAggregate));

        // Act
        Mono<DomainResponse> result = ourService.getAnAdvice(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertThat(response.getAnswer()).isEqualTo(expectedAdvice))
                .verifyComplete();

        verify(adviceAPI).getRandomAdvice();
    }

    @Test
    @DisplayName("getAnAdvice handles empty advice response")
    void getAnAdvice_HandlesEmptyAdviceResponse() {
        // Arrange
        DomainRequest request = DomainRequest.builder()
                .question("test")
                .build();
        MultiAggregate mockAggregate = MultiAggregate.builder()
                .answer("")
                .build();

        when(adviceAPI.getRandomAdvice()).thenReturn(Mono.just(mockAggregate));

        // Act
        Mono<DomainResponse> result = ourService.getAnAdvice(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> assertThat(response.getAnswer()).isEmpty())
                .verifyComplete();
    }

    @Test
    @DisplayName("getAnAdvice propagates error from AdviceApi")
    void getAnAdvice_PropagatesErrorFromAdviceApi() {
        // Arrange
        DomainRequest request = DomainRequest.builder()
                .question("test")
                .build();
        RuntimeException expectedError = new RuntimeException("External API failed");

        when(adviceAPI.getRandomAdvice()).thenReturn(Mono.error(expectedError));

        // Act
        Mono<DomainResponse> result = ourService.getAnAdvice(request);

        // Assert
        StepVerifier.create(result)
                .expectError(RuntimeException.class)
                .verify();

        verify(adviceAPI).getRandomAdvice();
    }

    @Test
    @DisplayName("getAnAdvice does not use domain request parameters")
    void getAnAdvice_DoesNotUseDomainRequestParameters() {
        // Arrange
        DomainRequest request = DomainRequest.builder()
                .question("this parameter is not used")
                .build();
        MultiAggregate mockAggregate = MultiAggregate.builder()
                .answer("API provides random advice")
                .build();

        when(adviceAPI.getRandomAdvice()).thenReturn(Mono.just(mockAggregate));

        // Act
        Mono<DomainResponse> result = ourService.getAnAdvice(request);

        // Assert
        StepVerifier.create(result)
                .assertNext(response -> {
                    assertThat(response.getAnswer()).isNotEqualTo("this parameter is not used");
                    assertThat(response.getAnswer()).isEqualTo("API provides random advice");
                })
                .verifyComplete();
    }
}
