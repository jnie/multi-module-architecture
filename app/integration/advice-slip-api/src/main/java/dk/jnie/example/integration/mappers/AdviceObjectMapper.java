package dk.jnie.example.integration.mappers;

import dk.jnie.example.advice.model.AdviceResponse;
import dk.jnie.example.model.MultiAggregate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import reactor.core.publisher.Mono;

@Mapper(componentModel = "spring")
public interface AdviceObjectMapper {

    @Mapping(target="answer", source="slip.advice")
    MultiAggregate toDomain(AdviceResponse response);

    default Mono<MultiAggregate> toDomainReactive(AdviceResponse response) {
        return Mono.just(toDomain(response));
    }
}
