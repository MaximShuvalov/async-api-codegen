package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

final class JavaModelRenderer {
    List<GeneratedSource> render(AsyncApiDocument document, String modelPackage) {
        List<GeneratedSource> result = new ArrayList<>();
        for (Map.Entry<String, JsonNode> schema : document.schemas().entrySet()) {
            String name = AsyncApiCodeGenerator.javaName(schema.getKey());
            result.add(source(modelPackage, name, renderType(modelPackage, name, schema.getValue())));
        }
        return result;
    }

    private String renderType(String basePackage, String name, JsonNode schema) {
        if (schema.path("enum").isArray()) {
            List<String> constants = new ArrayList<>();
            schema.path("enum").forEach(v -> constants.add(v.asText().replaceAll("[^A-Za-z0-9]", "_").toUpperCase()));
        return "package " + basePackage + ";\n\npublic enum " + name + " { " + String.join(", ", constants) + " }\n";
        }
        return "package " + basePackage + ";\n\npublic record " + name + "(" + fields(schema) + ") { }\n";
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
            case "string" -> isByteArray(schema) ? "byte[]" : "String";
            case "integer" -> schema.path("format").asText().equals("int64") ? "Long" : "Integer";
            case "number" -> schema.path("format").asText().equals("float") ? "Float" : "Double";
            case "boolean" -> "Boolean";
            case "array" -> "java.util.List<" + type(schema.path("items")) + ">";
            case "object" -> schema.has("additionalProperties") ? "java.util.Map<String, " + type(schema.path("additionalProperties")) + ">" : "java.util.Map<String, Object>";
            default -> "Object";
        };
    }

    private boolean isByteArray(JsonNode schema) {
        String format = schema.path("format").asText();
        return "byte".equals(format) || "binary".equals(format) || "base64".equals(schema.path("contentEncoding").asText());
    }

    private String safeField(String value) {
        return switch (value) { case "class", "record", "enum", "package", "public", "private" -> value + "Value"; default -> value; };
    }

    private GeneratedSource source(String packageName, String name, String content) {
        return new GeneratedSource(packageName.replace('.', '/') + "/" + name + ".java", content);
    }
}
