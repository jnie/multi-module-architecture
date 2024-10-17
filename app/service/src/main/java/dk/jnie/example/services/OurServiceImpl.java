package dk.jnie.example.services;

import dk.jnie.example.integration.AdviceAPI;
import dk.jnie.example.model.DomainRequest;
import dk.jnie.example.model.DomainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class OurServiceImpl implements OurService {
    private final AdviceAPI adviceAPI;
    @Override
    public Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest) {
        return adviceAPI.getRandomAdvice()
                .map(advice -> DomainResponse.builder()
                        .answer(advice)
                        .build());
    }
}
