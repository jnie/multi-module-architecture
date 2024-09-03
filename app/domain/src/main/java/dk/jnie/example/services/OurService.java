package dk.jnie.example.services;

import dk.jnie.example.model.DomainRequest;
import dk.jnie.example.model.DomainResponse;
import reactor.core.publisher.Mono;

public interface OurService {

    Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest);
}
