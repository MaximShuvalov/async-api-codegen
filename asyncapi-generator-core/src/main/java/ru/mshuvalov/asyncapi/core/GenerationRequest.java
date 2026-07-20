package ru.mshuvalov.asyncapi.core;

import java.nio.file.Path;
import java.util.Objects;

/** Input independent of any build tool. Transport is inferred from AsyncAPI bindings. */
public record GenerationRequest(Path specification, Path outputDirectory, GenerationOptions options) {
    public GenerationRequest {
        Objects.requireNonNull(specification, "specification");
        Objects.requireNonNull(outputDirectory, "outputDirectory");
        Objects.requireNonNull(options, "options");
    }
}
