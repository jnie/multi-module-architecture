package dk.jnie.example.services;

import dk.jnie.example.domain.outbound.AdviceApi;
import dk.jnie.example.domain.services.OurService;
import dk.jnie.example.domain.model.DomainRequest;
import dk.jnie.example.domain.model.DomainResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class OurServiceImpl implements OurService {
    private static final Logger log = LoggerFactory.getLogger(OurServiceImpl.class);
    
    private final AdviceApi adviceAPI;

    public OurServiceImpl(AdviceApi adviceAPI) {
        this.adviceAPI = adviceAPI;
    }

    @Override
    public Mono<DomainResponse> getAnAdvice(DomainRequest domainRequest) {
        log.debug("Requesting advice for: {}", domainRequest.getQuestion());
        return adviceAPI.getRandomAdvice()
                .map(advice -> DomainResponse.builder()
                        .answer(advice.getAnswer())
                        .build());
    }
}
