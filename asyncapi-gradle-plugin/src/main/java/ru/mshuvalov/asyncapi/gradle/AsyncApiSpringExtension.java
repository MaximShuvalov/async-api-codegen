package ru.mshuvalov.asyncapi.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/** Public `asyncApiSpring {}` DSL. */
public abstract class AsyncApiSpringExtension {
    public abstract RegularFileProperty getSpecification();
    public abstract Property<String> getBasePackage();
    public abstract Property<String> getKafkaPackage();
    public abstract Property<Boolean> getGenerateModels();
    public abstract Property<Boolean> getGenerateProducers();
    public abstract Property<Boolean> getGenerateConsumers();

    public void generateModel(boolean value) { getGenerateModels().set(value); }
    public void generateProducer(boolean value) { getGenerateProducers().set(value); }
    public void generateConsumer(boolean value) { getGenerateConsumers().set(value); }
    public void kafkaPackage(String value) { getKafkaPackage().set(value); }
}
