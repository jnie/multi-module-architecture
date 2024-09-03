package dk.jnie.example.util;

import org.immutables.value.Value;

@Value.Style(
        typeImmutable = "*", // Remove the "Immutable" prefix
        typeAbstract = "*Def" // Use "*Def" as the suffix for the abstract type
)
@Value.Immutable
public @interface ObjectStyle {
}