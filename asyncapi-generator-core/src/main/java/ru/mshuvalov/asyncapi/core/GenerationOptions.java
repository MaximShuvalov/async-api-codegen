package ru.mshuvalov.asyncapi.core;

import java.util.Objects;

/** Controls generated artifacts and their Java packages. */
public record GenerationOptions(
    String modelPackage,
    String contractPackage,
    String kafkaPackage,
    boolean generateModels,
    boolean generateContracts,
    boolean generateProducers,
    boolean generateConsumers
) {
    public GenerationOptions {
        requirePackage(modelPackage, "modelPackage");
        requirePackage(contractPackage, "contractPackage");
        requirePackage(kafkaPackage, "kafkaPackage");
        if (!generateContracts && (generateProducers || generateConsumers)) {
            throw new IllegalArgumentException("Contracts must be generated when producers or consumers are enabled");
        }
    }

    public static GenerationOptions defaults(String basePackage) {
        requirePackage(basePackage, "basePackage");
        return new GenerationOptions(basePackage + ".model", basePackage + ".contract", basePackage + ".kafka", true, true, true, true);
    }

    private static void requirePackage(String value, String name) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(name + " must not be blank");
    }
}
