package dk.jnie.example.integration;

import dk.jnie.example.model.MultiAggregate;
import reactor.core.publisher.Mono;

/**
 * AdviceApi is an interface that defines a contract for fetching random advice.
 * It returns a {@link Mono} that emits a {@link MultiAggregate}, which contains the aggregated response data.
 * This interface is typically used in a reactive programming context, like Spring WebFlux.
 */
public interface AdviceApi {

    /**
     * Fetches a random piece of advice.
     *
     * <p>This method is designed to be non-blocking and returns a {@link Mono},
     * which represents a single asynchronous computation. The Mono will emit a
     * {@link MultiAggregate} object that encapsulates the random advice data.</p>
     *
     * @return a {@link Mono} emitting the {@link MultiAggregate} object containing the advice data.
     *         The Mono completes when the advice is successfully fetched or with an error if the fetch fails.
     */
    Mono<MultiAggregate> getRandomAdvice();
}
