package io.kestra.core.models.flows;

import io.micronaut.core.annotation.Introspected;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

/**
 * Definition of a flow's output.
 */
@SuperBuilder
@Getter
@NoArgsConstructor
@Introspected
public class Output {
    /**
     * The output's unique id.
     */
    @NotNull
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z0-9][.a-zA-Z0-9_-]*")
    String id;
    /**
     * Short description of the output.
     */
    String description;
    /**
     * The output value. Can be a dynamic expression.
     */
    @NotNull
    Object value;
}
