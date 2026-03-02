package dk.jnie.example.advice;

import dk.jnie.example.model.MultiAggregate;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/**
 * Test controller that exposes AdviceApiImpl via HTTP GET endpoint.
 *
 * <p>This controller is only present during tests and allows WebTestClient
 * to make real HTTP calls through the full Spring stack to verify:
 * <ul>
 *   <li>WebTestClient HTTP exchange works</li>
 *   <li>WireMock intercepts the external API call</li>
 *   <li>AdviceApiImpl processes the response correctly</li>
 *   <li>The entire reactive pipeline completes successfully</li>
 * </ul>
 *
 * @see AdviceTestConfig
 * @see AdviceApiImplTest
 */
@RestController
@RequestMapping(path = "/api/advice", produces = MediaType.APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
public class AdviceTestController {

    private final AdviceApiImpl adviceApi;

    /**
     * GET endpoint that returns advice from the external API.
     *
     * <p>This triggers AdviceApiImpl.getRandomAdvice() which makes a real
     * HTTP call to the WireMock backend.</p>
     *
     * @return a Mono containing the MultiAggregate with the advice
     */
    @GetMapping
    public Mono<MultiAggregate> getAdvice() {
        return adviceApi.getRandomAdvice();
    }
}
