package dk.jnie.example.rest.mappers;

import dk.jnie.example.domain.model.DomainRequest;
import dk.jnie.example.domain.model.DomainResponse;
import dk.jnie.example.rest.model.RequestDto;
import dk.jnie.example.rest.model.ResponseDto;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestMapper {

    @Mapping(source = "answer", target = "advice")
    ResponseDto domainToResponseDto(DomainResponse domainResponse);


    @Mapping(source="please", target = "question")
    DomainRequest requestDTOToDomain(RequestDto requestDto);
}

