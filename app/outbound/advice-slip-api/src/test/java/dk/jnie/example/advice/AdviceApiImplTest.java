package dk.jnie.example.advice;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.core.WireMockConfiguration;
import dk.jnie.example.model.ImmutableMultiAggregate;
import dk.jnie.example.model.MultiAggregate;
import dk.jnie.example.model.MultiAggregateDef;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.reactive.server.WebTestClient;

import static com.github.tomakehurst.wiremock.client.WireMock.anyUrl;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.getRequestedFor;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.serverError;
import static com.github.tomakehurst.wiremock.client.WireMock.urlPathEqualTo;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for AdviceApiImpl using real WebTestClient calls with WireMock backend.
 *
 * <p>This test class starts a real Spring WebFlux application with WebTestClient,
 * while using WireMock to mock the external Advice Slip API. This provides:
 * <ul>
 *   <li>Real HTTP stack testing via WebTestClient</li>
 *   <li>Full Spring context with dependency injection</li>
 *   <li>WireMock verification of actual HTTP requests</li>
 *   <li>Deterministic, fast tests without external dependencies</li>
 * </ul>
 *
 * @see AdviceApiImpl
 * @see <a href="https://api.adviceslip.com">Advice Slip API</a>
 */
@SpringBootTest(
        classes = {AdviceTestConfig.class},
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
@DisplayName("AdviceApiImpl - WireMock + WebTestClient Integration Tests")
class AdviceApiImplTest {

    private static WireMockServer wireMockServer;

    @Autowired
    private WebTestClient webTestClient;

    @Autowired
    private AdviceApiImpl adviceApiImpl;

    /**
     * Configures the WireMock server URL as a dynamic property that will be injected
     * into the Spring context. This allows the WebClient bean to be configured with
     * the actual WireMock port at runtime.
     */
    @DynamicPropertySource
    static void registerWireMockUrl(DynamicPropertyRegistry registry) {
        wireMockServer = new WireMockServer(WireMockConfiguration.wireMockConfig().dynamicPort());
        wireMockServer.start();
        WireMock.configureFor(wireMockServer.port());

        registry.add("advice.api.base-url", wireMockServer::baseUrl);
    }

    @BeforeEach
    void setUp() {
        wireMockServer.resetAll();
    }

    @AfterEach
    void tearDown() {
        wireMockServer.resetAll();
    }

    /**
     * Tests successful retrieval of random advice through real WebTestClient call.
     *
     * <p>Verifies that:
     * <ul>
     *   <li>The WebTestClient makes a real HTTP call</li>
     *   <li>WireMock intercepts and returns the stubbed response</li>
     *   <li>AdviceApiImpl correctly parses the JSON response</li>
     *   <li>The endpoint returns the expected advice content</li>
     * </ul>
     */
    @Test
    @DisplayName("Should return valid advice from WireMock backend via WebTestClient")
    void getRandomAdvice_ReturnsValidAdvice() {
        // Arrange: Configure WireMock stub for the /advice endpoint
        String jsonResponse = """
            {
                "slip": {
                    "id": 125,
                    "advice": "Don't be afraid to ask questions."
                }
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(okJson(jsonResponse)
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_HTML_VALUE)));

        // Act & Assert: Make real HTTP call via WebTestClient
        webTestClient.get()
                .uri("/api/advice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MultiAggregate.class)
                .value(aggregate -> {
                    assertThat(aggregate.getAnswer())
                            .as("Advice text should match the WireMock stub response")
                            .isEqualTo("Don't be afraid to ask questions.");
                });

        // Verify: WireMock received the request
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice")));
    }

    /**
     * Tests the service layer directly with real WebClient calls through WireMock.
     *
     * <p>This test verifies the AdviceApiImpl service directly (not via HTTP endpoint),
     * ensuring it makes actual HTTP calls to the WireMock backend.</p>
     */
    @Test
    @DisplayName("Should call external API via real HTTP through WireMock")
    void getRandomAdvice_ServiceMakesRealHttpCall() {
        // Arrange
        String jsonResponse = """
            {
                "slip": {
                    "id": 42,
                    "advice": "Test advice from WireMock."
                }
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(okJson(jsonResponse)
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_HTML_VALUE)));

        // Act: Call the service directly - it should make real HTTP to WireMock
        MultiAggregate result = adviceApiImpl.getRandomAdvice().block();

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getAnswer()).isEqualTo("Test advice from WireMock.");

        // Verify the HTTP call was actually made
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice"))
                .withHeader("Accept", WireMock.containing("text/html")));
    }

    /**
     * Tests error handling when the external API returns HTTP 500.
     *
     * <p>Verifies that the application properly propagates HTTP errors through
     * the WebTestClient response.</p>
     */
    @Test
    @DisplayName("Should propagate HTTP 500 error from WireMock to WebTestClient")
    void getRandomAdvice_PropagatesHttpServerError() {
        // Arrange: Configure WireMock to return 500
        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(serverError()
                        .withBody("Internal Server Error")));

        // Act & Assert: Expect 5xx error response
        webTestClient.get()
                .uri("/api/advice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        // Verify the request was actually sent to WireMock
        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice")));
    }

    /**
     * Tests handling of HTTP 404 from the external API.
     */
    @Test
    @DisplayName("Should handle HTTP 404 from external API")
    void getRandomAdvice_HandlesNotFoundError() {
        // Arrange
        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(WireMock.notFound()
                        .withBody("Not Found")));

        // Act & Assert
        webTestClient.get()
                .uri("/api/advice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice")));
    }

    /**
     * Tests handling of malformed JSON from the external API.
     */
    @Test
    @DisplayName("Should handle malformed JSON response from external API")
    void getRandomAdvice_ThrowsExceptionForMalformedJson() {
        // Arrange
        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(WireMock.aResponse()
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_HTML_VALUE)
                        .withBody("{invalid json}")));

        // Act & Assert: The service should handle this gracefully
        webTestClient.get()
                .uri("/api/advice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError();

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice")));
    }

    /**
     * Tests that different stub responses are correctly handled.
     */
    @Test
    @DisplayName("Should handle different advice content from WireMock")
    void getRandomAdvice_ReturnsDifferentAdviceContent() {
        // Arrange
        String jsonResponse = """
            {
                "slip": {
                    "id": 74,
                    "advice": "Keep your options open."
                }
            }
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(okJson(jsonResponse)
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_HTML_VALUE)));

        // Act & Assert via WebTestClient
        webTestClient.get()
                .uri("/api/advice")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(MultiAggregate.class)
                .value(aggregate ->
                    assertThat(aggregate.getAnswer())
                            .as("Advice should match the configured WireMock stub")
                            .isEqualTo("Keep your options open."));

        wireMockServer.verify(getRequestedFor(urlPathEqualTo("/advice")));
    }

    /**
     * Verifies multiple requests hit WireMock correctly.
     */
    @Test
    @DisplayName("Should make separate HTTP calls for each WebTestClient request")
    void getRandomAdvice_MakesSeparateHttpCalls() {
        // Arrange
        String firstResponse = """
            {"slip": {"id": 1, "advice": "First advice."}}
            """;

        wireMockServer.stubFor(get(urlPathEqualTo("/advice"))
                .willReturn(okJson(firstResponse)
                        .withStatus(200)
                        .withHeader("Content-Type", MediaType.TEXT_HTML_VALUE)));

        // Act: Make two separate requests
        webTestClient.get()
                .uri("/api/advice")
                .exchange()
                .expectStatus().isOk();

        webTestClient.get()
                .uri("/api/advice")
                .exchange()
                .expectStatus().isOk();

        // Assert: WireMock received exactly 2 requests
        wireMockServer.verify(2, getRequestedFor(urlPathEqualTo("/advice")));
    }
}
