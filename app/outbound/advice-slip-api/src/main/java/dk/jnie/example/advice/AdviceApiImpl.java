package dk.jnie.example.advice;

import com.fasterxml.jackson.databind.ObjectMapper;
import dk.jnie.example.advice.mappers.AdviceObjectMapper;
import dk.jnie.example.advice.model.AdviceResponse;
import dk.jnie.example.domain.model.MultiAggregate;
import dk.jnie.example.domain.outbound.AdviceApi;
import dk.jnie.example.domain.repository.CacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Optional;

@Service
public class AdviceApiImpl implements AdviceApi {
    private static final Logger log = LoggerFactory.getLogger(AdviceApiImpl.class);

    private static final String CACHE_KEY = "advice:random";

    private final ObjectMapper objectMapper;
    private final AdviceObjectMapper mapper;
    private final WebClient adviceWebClient;
    private final Optional<CacheRepository> cacheRepository;
    private final boolean cacheEnabled;

    @Autowired
    public AdviceApiImpl(ObjectMapper objectMapper,
                         AdviceObjectMapper mapper,
                         WebClient adviceWebClient,
                         @Autowired(required = false) CacheRepository cacheRepository,
                         @Value("${mma.cache.enabled:true}") boolean cacheEnabled) {
        this.objectMapper = objectMapper;
        this.mapper = mapper;
        this.adviceWebClient = adviceWebClient;
        this.cacheRepository = Optional.ofNullable(cacheRepository);
        this.cacheEnabled = cacheEnabled;
    }

    @Override
    public Mono<MultiAggregate> getRandomAdvice() {
        if (cacheEnabled && cacheRepository.isPresent()) {
            return getFromCache().switchIfEmpty(fetchAndCache());
        }
        return fetchFromApi();
    }

    private Mono<MultiAggregate> getFromCache() {
        return cacheRepository.get().get(CACHE_KEY)
                .flatMap(this::deserializeFromCache)
                .doOnNext(aggregate -> log.info("Retrieved advice from cache"));
    }

    private Mono<MultiAggregate> fetchAndCache() {
        return fetchFromApi()
                .flatMap(aggregate -> cacheRepository.get().put(CACHE_KEY, serializeForCache(aggregate))
                        .thenReturn(aggregate));
    }

    private Mono<MultiAggregate> fetchFromApi() {
        log.info("Calling the advice API");

        return adviceWebClient.get()
                .uri("/advice")
                .accept(MediaType.TEXT_HTML)
                .retrieve()
                .bodyToMono(String.class)
                .flatMap(this::convertToAdviceResponse)
                .flatMap(mapper::toDomainReactive);
    }

    private Mono<MultiAggregate> deserializeFromCache(String cachedValue) {
        try {
            return Mono.just(objectMapper.readValue(cachedValue, MultiAggregate.class));
        } catch (Exception e) {
            log.warn("Failed to deserialize cached value, fetching fresh data", e);
            return Mono.empty();
        }
    }

    private String serializeForCache(MultiAggregate aggregate) {
        try {
            return objectMapper.writeValueAsString(aggregate);
        } catch (Exception e) {
            log.error("Failed to serialize aggregate for caching", e);
            return null;
        }
    }

    private Mono<AdviceResponse> convertToAdviceResponse(String responseBody) {
        try {
            AdviceResponse adviceResponse = objectMapper.readValue(responseBody, AdviceResponse.class);
            return Mono.just(adviceResponse);
        } catch (Exception e) {
            return Mono.error(new RuntimeException("Failed to parse response body as JSON", e));
        }
    }
}