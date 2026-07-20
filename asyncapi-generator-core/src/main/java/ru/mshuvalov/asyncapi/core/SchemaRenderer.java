package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

/** Renders one schema language into Java types; independent from messaging transports. */
interface SchemaRenderer {
    SchemaFormat format();
    List<GeneratedSource> render(Map<String, JsonNode> schemas, GenerationOptions options);
}
