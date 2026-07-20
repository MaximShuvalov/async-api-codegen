package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;

final class ContractRenderer {
    List<GeneratedSource> render(AsyncApiDocument document, String contractPackage, String modelPackage) {
        List<GeneratedSource> sources = new ArrayList<>();
        sources.add(new GeneratedSource(path(contractPackage, "MessageMetadata"), "package " + contractPackage + ";\n\npublic record MessageMetadata(String topic, String key) { }\n"));
        sources.add(new GeneratedSource(path(contractPackage, "Channels"), renderChannels(document, contractPackage)));
        for (AsyncApiDocument.Operation op : document.operations()) {
            String payload = payloadType(op.payload());
            String headers = op.messageName() + "Headers";
            String name = op.messageName() + (op.action() == AsyncApiDocument.Action.SEND ? "Publisher" : "Handler");
            String method = op.action() == AsyncApiDocument.Action.SEND
                ? "void send(" + payload + " payload, " + headers + " headers);"
                : "void handle(" + payload + " payload, " + headers + " headers, MessageMetadata metadata);";
            String modelImport = op.payload().has("$ref") ? "import " + modelPackage + "." + payload + ";\n" : "";
            String content = "package " + contractPackage + ";\n\n" + modelImport + "\npublic interface " + name + " {\n    " + method + "\n}\n";
            sources.add(new GeneratedSource(path(contractPackage, name), content));
            String headerContent = "package " + contractPackage + ";\n\npublic record " + headers + "(" + headerFields(op.headers()) + ") { }\n";
            sources.add(new GeneratedSource(path(contractPackage, headers), headerContent));
        }
        return sources;
    }

    static String payloadType(JsonNode payload) {
        if (payload.has("$ref")) {
            String ref = payload.path("$ref").asText();
            return "" + AsyncApiCodeGenerator.javaName(ref.substring(ref.lastIndexOf('/') + 1));
        }
        if (payload.has("schemaFormat")) {
            JsonNode schema = payload.path("schema");
            if (SchemaFormat.from(payload.path("schemaFormat").asText()) == SchemaFormat.AVRO) {
                return AsyncApiCodeGenerator.javaName(schema.path("name").asText("Object"));
            }
        }
        return "Object";
    }
    private static String headerFields(JsonNode headers) {
        List<String> fields = new ArrayList<>();
        headers.path("properties").fields().forEachRemaining(e -> fields.add(headerType(e.getValue()) + " " + e.getKey()));
        return String.join(", ", fields);
    }
    static String channelConstant(String topic) {
        String constant = topic.replaceAll("[^A-Za-z0-9]", "_").replaceAll("_+", "_").toUpperCase(java.util.Locale.ROOT);
        if (constant.isEmpty() || Character.isDigit(constant.charAt(0))) constant = "CHANNEL_" + constant;
        return constant;
    }
    private String renderChannels(AsyncApiDocument document, String contractPackage) {
        java.util.Map<String, String> names = new java.util.LinkedHashMap<>();
        java.util.Set<String> used = new java.util.HashSet<>();
        for (AsyncApiDocument.Operation operation : document.operations()) {
            if (names.containsKey(operation.topic())) continue;
            String base = channelConstant(operation.topic());
            if (!used.add(base)) throw new IllegalArgumentException("Channel constant name collision for topic: " + operation.topic());
            names.put(operation.topic(), base);
        }
        StringBuilder constants = new StringBuilder();
        names.forEach((topic, name) -> constants.append("    public static final String ").append(name).append(" = \"")
            .append(topic.replace("\\", "\\\\").replace("\"", "\\\"")).append("\";\n"));
        return "package " + contractPackage + ";\n\npublic final class Channels {\n    private Channels() { }\n\n" + constants + "}\n";
    }
    private static String headerType(JsonNode schema) {
        return switch (schema.path("type").asText("object")) {
            case "string" -> isByteArray(schema) ? "byte[]" : "String";
            case "integer" -> "Long";
            case "number" -> "Double";
            case "boolean" -> "Boolean";
            default -> "Object";
        };
    }
    private static boolean isByteArray(JsonNode schema) {
        String format = schema.path("format").asText();
        return "byte".equals(format) || "binary".equals(format) || "base64".equals(schema.path("contentEncoding").asText());
    }
    static String path(String packageName, String name) { return packageName.replace('.', '/') + "/" + name + ".java"; }
}
