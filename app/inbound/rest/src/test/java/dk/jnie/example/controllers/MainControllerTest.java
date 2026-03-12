package dk.jnie.example.controllers;

import dk.jnie.example.rest.controllers.MainController;
import dk.jnie.example.rest.mappers.RestMapper;
import dk.jnie.example.domain.model.DomainRequest;
import dk.jnie.example.domain.model.DomainResponse;
import dk.jnie.example.rest.model.RequestDto;
import dk.jnie.example.rest.model.ResponseDto;
import dk.jnie.example.domain.services.OurService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for MainController.
 * Uses WebTestClient to test the REST endpoints without starting the full application context.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MainController Tests")
class MainControllerTest {

    @Mock
    private OurService ourServiceMock;

    @Mock
    private RestMapper restMapperMock;

    @InjectMocks
    private MainController mainController;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(mainController).build();
    }

    @Test
    @DisplayName("POST /api/v1/advice returns successful response")
    void getAdvice_ReturnsSuccessfulResponse() {
        // Arrange
        RequestDto requestDto = new RequestDto();
        requestDto.setPlease("anything");

        ResponseDto responseDto = new ResponseDto();
        responseDto.setAdvice("Don't be afraid to ask questions.");

        DomainResponse domainResponse = DomainResponse.builder()
                .answer("Don't be afraid to ask questions.")
                .build();

        when(restMapperMock.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(DomainRequest.builder().question("anything").build());
        when(ourServiceMock.getAnAdvice(any())).thenReturn(Mono.just(domainResponse));
        when(restMapperMock.domainToResponseDto(any(DomainResponse.class))).thenReturn(responseDto);

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"please\":\"anything\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ResponseDto.class)
                .value(response -> assertThat(response.getAdvice()).isEqualTo("Don't be afraid to ask questions."));

        verify(ourServiceMock).getAnAdvice(any());
    }

    @Test
    @DisplayName("POST /api/v1/advice handles empty request")
    void getAdvice_HandlesEmptyRequest() {
        // Arrange
        RequestDto requestDto = new RequestDto();
        requestDto.setPlease(null);

        ResponseDto responseDto = new ResponseDto();
        responseDto.setAdvice("Default advice");

        DomainResponse domainResponse = DomainResponse.builder()
                .answer("Default advice")
                .build();

        when(restMapperMock.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(DomainRequest.builder().question("null").build());
        when(ourServiceMock.getAnAdvice(any())).thenReturn(Mono.just(domainResponse));
        when(restMapperMock.domainToResponseDto(any(DomainResponse.class))).thenReturn(responseDto);

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("POST /api/v1/advice returns 500 on service error")
    void getAdvice_Returns500OnServiceError() {
        // Arrange
        when(restMapperMock.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(DomainRequest.builder().question("test").build());
        when(ourServiceMock.getAnAdvice(any())).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/v1/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"please\":\"test\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
