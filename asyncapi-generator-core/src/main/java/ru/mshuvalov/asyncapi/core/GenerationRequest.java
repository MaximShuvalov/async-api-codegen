package ru.mshuvalov.asyncapi.core;

import java.nio.file.Path;
import java.util.Objects;

/** Input independent of any build tool. */
public record GenerationRequest(Path specification, Path outputDirectory, String basePackage, String transport) {
    public GenerationRequest {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        if (basePackage == null || basePackage.isBlank()) throw new IllegalArgumentException("basePackage must not be blank");
        if (!"kafka".equalsIgnoreCase(transport)) throw new IllegalArgumentException("Only kafka transport is supported by this version");
    }
}
