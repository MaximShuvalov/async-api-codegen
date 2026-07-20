package ru.mshuvalov.asyncapi.gradle;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

/** Public `asyncApiSpring {}` DSL. */
public abstract class AsyncApiSpringExtension {
    public abstract RegularFileProperty getSpecification();
    public abstract Property<String> getBasePackage();
    public abstract Property<String> getTransport();
}
