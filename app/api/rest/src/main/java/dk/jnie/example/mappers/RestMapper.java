package dk.jnie.example.mappers;

import dk.jnie.example.model.DomainRequest;
import dk.jnie.example.model.DomainResponse;
import dk.jnie.example.model.RequestDto;
import dk.jnie.example.model.ResponseDto;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestMapper {

    //@Mapping(source = "answer", target = "advice")
    ResponseDto domainToResponseDto(DomainResponse domainResponse);


    //@Mapping(source="please", target = "question")
    DomainRequest requestDTOToDomain(RequestDto requestDto);
}

