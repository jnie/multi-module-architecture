package dk.jnie.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class ResponseDto {
    @JsonProperty("advice")
    String advice;
}
