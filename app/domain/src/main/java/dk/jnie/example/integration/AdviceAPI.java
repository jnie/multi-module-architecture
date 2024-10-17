package dk.jnie.example.integration;

import reactor.core.publisher.Mono;

public interface AdviceAPI {

    Mono<String> getRandomAdvice();

}
