package dk.jnie.example.controllers;

import dk.jnie.example.mappers.RestMapper;
import dk.jnie.example.model.RequestDto;
import dk.jnie.example.model.ResponseDto;
import dk.jnie.example.services.OurService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping(path = "/api/advice",
        consumes = MediaType.APPLICATION_JSON_VALUE,
        produces = MediaType.APPLICATION_JSON_VALUE)
@Tag(name = "Main API", description = "Call for an advice, this could f.ex be a random advice")
public class MainController {

    private final OurService ourService;
    private final RestMapper restMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Ask for an advice",
            description = "Usage, use POST verb and the request model to ask for an advice")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(schema = @Schema(implementation = RequestDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public Mono<ResponseDto> getAdvice(
            @Validated @RequestBody
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                                            {
                                              "please": "anything"
                                            }
                                            """
                            )
                    )
            )
            RequestDto request) {
        return ourService.getAnAdvice(restMapper.requestDTOToDomain(request))
                .map(restMapper::domainToResponseDto);
    }


}