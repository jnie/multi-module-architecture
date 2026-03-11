package dk.jnie.example.domain.services;

import dk.jnie.example.domain.model.DomainRequest;
import dk.jnie.example.domain.model.DomainResponse;
import reactor.core.publisher.Mono;

public interface OurService {

    Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest);
}
