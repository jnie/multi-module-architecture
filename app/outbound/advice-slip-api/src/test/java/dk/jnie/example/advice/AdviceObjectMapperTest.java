package dk.jnie.example.advice;

import dk.jnie.example.advice.mappers.AdviceObjectMapper;
import dk.jnie.example.advice.mappers.AdviceObjectMapperImpl;
import dk.jnie.example.advice.model.AdviceResponse;
import dk.jnie.example.advice.model.AdviceResponseSlip;
import dk.jnie.example.domain.model.MultiAggregate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AdviceObjectMapper Tests")
class AdviceObjectMapperTest {

    private AdviceObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new AdviceObjectMapperImpl();
    }

    @Test
    @DisplayName("toDomain should map AdviceResponse to MultiAggregate")
    void toDomain_ShouldMapAdviceResponseToMultiAggregate() {
        AdviceResponseSlip slip = new AdviceResponseSlip();
        slip.setAdvice("Test advice");

        AdviceResponse response = new AdviceResponse();
        response.setSlip(slip);

        MultiAggregate result = mapper.toDomain(response);

        assertThat(result).isNotNull();
        assertThat(result.getAnswer()).isEqualTo("Test advice");
    }

    @Test
    @DisplayName("toDomainReactive should wrap result in Mono")
    void toDomainReactive_ShouldWrapResultInMono() {
        AdviceResponseSlip slip = new AdviceResponseSlip();
        slip.setAdvice("Reactive advice");

        AdviceResponse response = new AdviceResponse();
        response.setSlip(slip);

        StepVerifier.create(mapper.toDomainReactive(response))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.getAnswer()).isEqualTo("Reactive advice");
                })
                .verifyComplete();
    }
}