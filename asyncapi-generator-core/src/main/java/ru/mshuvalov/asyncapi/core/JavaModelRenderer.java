package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class JavaModelRenderer {
    private final JsonNode root;
    JavaModelRenderer(JsonNode root) { this.root = root; }

    List<GeneratedSource> render(AsyncApiDocument document, String basePackage) {
        List<GeneratedSource> result = new ArrayList<>();
        for (Map.Entry<String, JsonNode> schema : document.schemas().entrySet()) {
            String name = AsyncApiCodeGenerator.javaName(schema.getKey());
            result.add(source(basePackage, "model", name, renderType(basePackage, name, schema.getValue())));
        }
        for (AsyncApiDocument.Operation operation : document.operations()) {
            String headers = operation.messageName() + "Headers";
            result.add(source(basePackage, "contract", headers, renderHeaders(basePackage, headers, operation.headers())));
        }
        return result;
    }

    private String renderType(String basePackage, String name, JsonNode schema) {
        if (schema.path("enum").isArray()) {
            List<String> constants = new ArrayList<>();
            schema.path("enum").forEach(v -> constants.add(v.asText().replaceAll("[^A-Za-z0-9]", "_").toUpperCase()));
            return "package " + basePackage + ".model;\n\npublic enum " + name + " { " + String.join(", ", constants) + " }\n";
        }
        return "package " + basePackage + ".model;\n\npublic record " + name + "(" + fields(schema) + ") { }\n";
    }

    private String renderHeaders(String basePackage, String name, JsonNode headers) {
        return "package " + basePackage + ".contract;\n\npublic record " + name + "(" + fields(headers) + ") { }\n";
    }

    private String fields(JsonNode schema) {
        List<String> fields = new ArrayList<>();
        JsonNode properties = schema.path("properties");
        properties.fields().forEachRemaining(e -> fields.add(type(e.getValue()) + " " + safeField(e.getKey())));
        return String.join(", ", fields);
    }

    private String type(JsonNode schema) {
        if (schema.has("$ref")) {
            String ref = schema.path("$ref").asText();
            return AsyncApiCodeGenerator.javaName(ref.substring(ref.lastIndexOf('/') + 1));
        }
        if (schema.has("enum")) return AsyncApiCodeGenerator.javaName(schema.path("title").asText("Value"));
        return switch (schema.path("type").asText("object")) {
            case "string" -> "String";
            case "integer" -> schema.path("format").asText().equals("int64") ? "Long" : "Integer";
            case "number" -> schema.path("format").asText().equals("float") ? "Float" : "Double";
            case "boolean" -> "Boolean";
            case "array" -> "java.util.List<" + type(schema.path("items")) + ">";
            case "object" -> schema.has("additionalProperties") ? "java.util.Map<String, " + type(schema.path("additionalProperties")) + ">" : "java.util.Map<String, Object>";
            default -> "Object";
        };
    }

    private String safeField(String value) {
        return switch (value) { case "class", "record", "enum", "package", "public", "private" -> value + "Value"; default -> value; };
    }

    private GeneratedSource source(String base, String part, String name, String content) {
        return new GeneratedSource(base.replace('.', '/') + "/" + part + "/" + name + ".java", content);
    }
}
