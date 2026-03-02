package dk.jnie.example.controllers;

import dk.jnie.example.mappers.RestMapper;
import dk.jnie.example.model.DomainResponse;
import dk.jnie.example.model.RequestDto;
import dk.jnie.example.model.ResponseDto;
import dk.jnie.example.services.OurService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
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
    private OurService ourService;

    @Mock
    private RestMapper restMapper;

    @InjectMocks
    private MainController mainController;

    private WebTestClient webTestClient;

    @org.junit.jupiter.api.BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(mainController).build();
    }

    @Test
    @DisplayName("POST /api/advice returns successful response")
    void getAdvice_ReturnsSuccessfulResponse() {
        // Arrange
        RequestDto requestDto = new RequestDto();
        requestDto.setPlease("anything");

        ResponseDto responseDto = new ResponseDto();
        responseDto.setAdvice("Don't be afraid to ask questions.");

        DomainResponse domainResponse = ImmutableDomainResponse.builder()
                .answer("Don't be afraid to ask questions.")
                .build();

        when(restMapper.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(ImmutableDomainRequest.builder().question("anything").build());
        when(ourService.getAnAdvice(any())).thenReturn(Mono.just(domainResponse));
        when(restMapper.domainToResponseDto(any(DomainResponse.class))).thenReturn(responseDto);

        // Act & Assert
        webTestClient.post()
                .uri("/api/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"please\":\"anything\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody(ResponseDto.class)
                .value(response -> {
                    assertThat(response.getAdvice()).isEqualTo("Don't be afraid to ask questions.");
                });

        verify(ourService).getAnAdvice(any());
    }

    @Test
    @DisplayName("POST /api/advice handles empty request")
    void getAdvice_HandlesEmptyRequest() {
        // Arrange
        RequestDto requestDto = new RequestDto();
        requestDto.setPlease(null);

        ResponseDto responseDto = new ResponseDto();
        responseDto.setAdvice("Default advice");

        DomainResponse domainResponse = ImmutableDomainResponse.builder()
                .answer("Default advice")
                .build();

        when(restMapper.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(ImmutableDomainRequest.builder().question(null).build());
        when(ourService.getAnAdvice(any())).thenReturn(Mono.just(domainResponse));
        when(restMapper.domainToResponseDto(any(DomainResponse.class))).thenReturn(responseDto);

        // Act & Assert
        webTestClient.post()
                .uri("/api/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{}")
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    @DisplayName("POST /api/advice returns 500 on service error")
    void getAdvice_Returns500OnServiceError() {
        // Arrange
        when(restMapper.requestDTOToDomain(any(RequestDto.class)))
                .thenReturn(ImmutableDomainRequest.builder().question("test").build());
        when(ourService.getAnAdvice(any())).thenReturn(Mono.error(new RuntimeException("Service unavailable")));

        // Act & Assert
        webTestClient.post()
                .uri("/api/advice")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"please\":\"test\"}")
                .exchange()
                .expectStatus().is5xxServerError();
    }
}
