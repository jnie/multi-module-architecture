package dk.jnie.example.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public record RequestDto(
    @JsonProperty("please") String please
) {}