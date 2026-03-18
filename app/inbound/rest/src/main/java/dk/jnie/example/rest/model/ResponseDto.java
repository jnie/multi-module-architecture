package dk.jnie.example.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ResponseDto(
    @JsonProperty("advice") String advice
) {}