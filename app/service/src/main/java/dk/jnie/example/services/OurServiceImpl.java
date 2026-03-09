package dk.jnie.example.services;

import dk.jnie.example.outbound.AdviceApi;
import dk.jnie.example.model.DomainRequest;
import dk.jnie.example.model.DomainResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@Slf4j
@Service
public class OurServiceImpl implements OurService {
    private final AdviceApi adviceAPI;
    @Override
    public Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest) {
        log.debug("Requesting advice for: {}", domainRequest.getQuestion());
        return adviceAPI.getRandomAdvice()
                .map(advice -> DomainResponse.builder()
                        .answer(advice.getAnswer())
                        .build());
    }
}
