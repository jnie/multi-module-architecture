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
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
@Tag(name = "Main Management", description = "API for managing our operations")
public class MainController {

    private final OurService ourService;
    private final RestMapper restMapper;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new user",
            description = "Creates a new user with the provided information")
    @ApiResponse(responseCode = "201", description = "User created successfully",
            content = @Content(schema = @Schema(implementation = RequestDto.class)))
    @ApiResponse(responseCode = "400", description = "Invalid input")
    public Mono<ResponseDto> getAdvice(
            @Validated @RequestBody(required = true)
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = RequestDto.class),
                            examples = @ExampleObject(
                                    value = """
                        {
                          "username": "johndoe",
                          "email": "john.doe@example.com",
                          "firstName": "John",
                          "lastName": "Doe",
                          "age": 30
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