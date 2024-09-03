package dk.jnie.example.model;

import dk.jnie.example.util.ObjectStyle;
import org.immutables.value.Value;

@ObjectStyle
@Value.Immutable
public interface DomainRequestDef {
    String getQuestion();
}
