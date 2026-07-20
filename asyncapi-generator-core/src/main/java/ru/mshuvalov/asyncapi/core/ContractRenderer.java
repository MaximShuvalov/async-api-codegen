package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;

final class ContractRenderer {
    List<GeneratedSource> render(AsyncApiDocument document, String basePackage) {
        List<GeneratedSource> sources = new ArrayList<>();
        sources.add(new GeneratedSource(path(basePackage, "MessageMetadata"), "package " + basePackage + ".contract;\n\npublic record MessageMetadata(String topic, String key) { }\n"));
        for (AsyncApiDocument.Operation op : document.operations()) {
            String payload = payloadType(op.payload());
            String headers = op.messageName() + "Headers";
            String name = op.messageName() + (op.action() == AsyncApiDocument.Action.SEND ? "Publisher" : "Handler");
            String method = op.action() == AsyncApiDocument.Action.SEND
                ? "void send(" + payload + " payload, " + headers + " headers);"
                : "void handle(" + payload + " payload, " + headers + " headers, MessageMetadata metadata);";
            String modelImport = op.payload().has("$ref") ? "import " + basePackage + ".model." + payload + ";\n" : "";
            String content = "package " + basePackage + ".contract;\n\n" + modelImport + "\npublic interface " + name + " {\n    " + method + "\n}\n";
            sources.add(new GeneratedSource(path(basePackage, name), content));
        }
        return sources;
    }

    static String payloadType(JsonNode payload) {
        if (payload.has("$ref")) {
            String ref = payload.path("$ref").asText();
            return "" + AsyncApiCodeGenerator.javaName(ref.substring(ref.lastIndexOf('/') + 1));
        }
        return "Object";
    }
    static String path(String base, String name) { return base.replace('.', '/') + "/contract/" + name + ".java"; }
}
