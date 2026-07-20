package ru.mshuvalov.asyncapi.core.model;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.List;
import java.util.Map;
import java.util.Set;

public record AsyncApiDocument(Map<String, JsonNode> schemas, List<Operation> operations, Set<String> transports) {
    public record Operation(String name, Action action, String topic, String messageName, JsonNode payload, JsonNode headers) { }
    public enum Action { SEND, RECEIVE }
}
