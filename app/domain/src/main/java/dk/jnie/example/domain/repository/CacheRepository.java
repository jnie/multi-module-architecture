package dk.jnie.example.domain.repository;

import reactor.core.publisher.Mono;

public interface CacheRepository {

    Mono<String> get(String key);

    Mono<Boolean> put(String key, String value);

    Mono<Boolean> evict(String key);

    Mono<Boolean> exists(String key);
}