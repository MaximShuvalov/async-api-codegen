package ru.mshuvalov.asyncapi.core;

import java.nio.file.Path;
import java.util.Objects;

/** Input independent of any build tool. Transport is inferred from AsyncAPI bindings. */
public record GenerationRequest(Path specification, Path outputDirectory, String basePackage) {
    public GenerationRequest {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        if (basePackage == null || basePackage.isBlank()) throw new IllegalArgumentException("basePackage must not be blank");
    }
}
