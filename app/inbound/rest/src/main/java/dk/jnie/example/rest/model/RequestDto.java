package dk.jnie.example.rest.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RequestDto {
    @JsonProperty("please")
    String please;
}
