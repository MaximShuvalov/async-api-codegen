package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.List;

/** Extension point for transport-specific adapters. */
public interface TransportRenderer {
    TransportType transport();
    List<GeneratedSource> render(AsyncApiDocument document, GenerationOptions options);
}
