package dk.jnie.example.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.jnie.example.advice.mappers.AdviceObjectMapper;
import dk.jnie.example.advice.model.AdviceResponse;
import dk.jnie.example.domain.model.MultiAggregate;
import dk.jnie.example.domain.outbound.AdviceApi;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Slf4j
@RequiredArgsConstructor
@Service
public class AdviceApiImpl implements AdviceApi {

    private final ObjectMapper objectMapper;
    private final AdviceObjectMapper mapper;
    private final WebClient adviceWebClient;

    @Override
    public Mono<MultiAggregate> getRandomAdvice() {
        log.info("Calling the advice API");

        return adviceWebClient.get()
                .uri("/advice")
                .accept(MediaType.TEXT_HTML) // Expecting text/html but it contains JSON data
                .retrieve()
                .bodyToMono(String.class) // Read response as raw string (because it's `text/html`)
                .flatMap(this::convertToAdviceResponse) // Convert string to JSON object
                .flatMap(mapper::toDomainReactive);
    }

    private Mono<AdviceResponse> convertToAdviceResponse(String responseBody) {
        try {
            // Parse the string into an AdviceResponse object
            AdviceResponse adviceResponse = objectMapper.readValue(responseBody, AdviceResponse.class);
            return Mono.just(adviceResponse);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to parse response body as JSON", e));
        }
    }
}
