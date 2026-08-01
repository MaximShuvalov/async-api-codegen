package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.TransportType;

public interface SourceGenerator {
    public TransportType getTransport();
    public String generate(SourceGeneratorInputData data);
}
