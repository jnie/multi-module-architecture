package dk.jnie.example.integration;

import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;

@Slf4j
public class AdviceAPIImpl implements AdviceAPI {
    @Override
    public Mono<String> getRandomAdvice() {
        log.error("Not implemented yet!");
        return Mono.just("Not implemented yet");
    }
}
