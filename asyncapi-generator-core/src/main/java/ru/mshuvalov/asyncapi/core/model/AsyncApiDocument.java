package ru.mshuvalov.asyncapi.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AsyncApiDocument(Map<String, JsonNode> schemas, List<Operation> operations) {
    public AsyncApiDocument {
        schemas = Map.copyOf(schemas);
        operations = List.copyOf(operations);
    }

    public record Operation(String name, Action action, String topic, String messageName, JsonNode payload, JsonNode headers,
                            Set<String> transports, SchemaFormat payloadFormat) {
        public Operation {
            transports = Set.copyOf(transports);
        }
    }

    public enum Action { SEND, RECEIVE }
}
