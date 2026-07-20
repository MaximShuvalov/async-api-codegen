package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Minimal native Avro-to-record renderer for inline AsyncAPI Avro schemas. */
final class AvroModelRenderer implements SchemaRenderer {
    @Override public SchemaFormat format() { return SchemaFormat.AVRO; }

    @Override public List<GeneratedSource> render(Map<String, JsonNode> schemas, GenerationOptions options) {
        List<GeneratedSource> result = new ArrayList<>();
        for (Map.Entry<String, JsonNode> entry : schemas.entrySet()) {
            JsonNode wrapper = entry.getValue();
            if (SchemaFormat.from(wrapper.path("schemaFormat").asText()) != format()) continue;
            JsonNode schema = wrapper.path("schema");
            String name = AsyncApiCodeGenerator.javaName(schema.path("name").asText(entry.getKey()));
            if ("record".equals(schema.path("type").asText())) {
                List<String> fields = new ArrayList<>();
                schema.path("fields").forEach(field -> fields.add(type(field.path("type")) + " " + field.path("name").asText()));
                result.add(source(options.modelPackage(), name, "package " + options.modelPackage() + ";\n\npublic record " + name + "(" + String.join(", ", fields) + ") { }\n"));
            } else if ("enum".equals(schema.path("type").asText())) {
                List<String> symbols = new ArrayList<>();
                schema.path("symbols").forEach(symbol -> symbols.add(symbol.asText()));
                result.add(source(options.modelPackage(), name, "package " + options.modelPackage() + ";\n\npublic enum " + name + " { " + String.join(", ", symbols) + " }\n"));
            } else throw new IllegalArgumentException("Unsupported Avro component type for " + entry.getKey() + ": " + schema.path("type").asText());
        }
        return result;
    }

    private String type(JsonNode node) {
        if (node.isArray()) { // nullable Avro unions and simple unions
            for (JsonNode alternative : node) if (!"null".equals(alternative.asText())) return type(alternative);
            return "Object";
        }
        if (node.isObject()) {
            return switch (node.path("type").asText()) {
                case "array" -> "java.util.List<" + type(node.path("items")) + ">";
                case "map" -> "java.util.Map<String, " + type(node.path("values")) + ">";
                default -> AsyncApiCodeGenerator.javaName(node.path("name").asText("Object"));
            };
        }
        return switch (node.asText()) {
            case "string" -> "String";
            case "bytes" -> "byte[]";
            case "int" -> "Integer";
            case "long" -> "Long";
            case "float" -> "Float";
            case "double" -> "Double";
            case "boolean" -> "Boolean";
            default -> AsyncApiCodeGenerator.javaName(node.asText());
        };
    }
    private GeneratedSource source(String packageName, String name, String content) {
        return new GeneratedSource(packageName.replace('.', '/') + "/" + name + ".java", content);
    }
}
