package ru.mshuvalov.asyncapi.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;

public record AsyncApiDocument(Map<String, JsonNode> schemas, List<Operation> operations) {
    public record Operation(String name, Action action, String topic, String messageName, JsonNode payload, JsonNode headers,
                            java.util.Set<String> transports, SchemaFormat payloadFormat) { }
    public enum Action { SEND, RECEIVE }
}
