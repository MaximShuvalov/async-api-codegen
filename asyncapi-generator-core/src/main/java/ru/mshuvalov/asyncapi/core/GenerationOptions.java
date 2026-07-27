package ru.mshuvalov.asyncapi.core;

/** Controls generated artifacts and their Java packages. */
public record GenerationOptions(
    String kafkaPackage,
    boolean generateModels,
    boolean generateProducers,
    boolean generateConsumers
) {
    public GenerationOptions {
        requirePackage(kafkaPackage, "kafkaPackage");
    }

    public static GenerationOptions defaults(String basePackage) {
        requirePackage(basePackage, "basePackage");
        return new GenerationOptions(basePackage + ".kafka", true, true, true);
    }

    public String dtoPackage() { return kafkaPackage + ".dto"; }

    private static void requirePackage(String value, String name) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(name + " must not be blank");
        }
    }
}
