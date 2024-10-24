package dk.jnie.example.services;

import dk.jnie.example.integration.AdviceApi;
import dk.jnie.example.model.DomainRequest;
import dk.jnie.example.model.DomainResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Service
public class OurServiceImpl implements OurService {
    private final AdviceApi adviceAPI;
    @Override
    public Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest) {
        return adviceAPI.getRandomAdvice()
                .map(advice -> DomainResponse.builder()
                        .answer(advice.getAnswer())
                        .build());
    }
}
