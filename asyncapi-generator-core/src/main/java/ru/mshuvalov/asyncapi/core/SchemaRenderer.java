package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mshuvalov.asyncapi.core.model.SchemaFormat;

import java.util.List;
import java.util.Map;
import java.util.Set;

/** Renders one schema language into Java types; independent from messaging transports. */
interface SchemaRenderer {
    Set<SchemaFormat> supportedFormats();
    List<GeneratedSource> render(Map<String, JsonNode> schemas, GenerationOptions options);
}
