package ru.mshuvalov.asyncapi.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument.Action;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/** Parses AsyncAPI 3.0 into a renderer-facing model and writes Java sources. */
public final class AsyncApiCodeGenerator {
    private final ObjectMapper mapper = new ObjectMapper(new YAMLFactory());
    private final Map<TransportType, TransportRenderer> transportRenderers;

    public AsyncApiCodeGenerator() {
        this(List.of(new KafkaTransportRenderer()));
    }

    /** Enables CLI/Maven/IDE integrations to provide additional transport renderers. */
    public AsyncApiCodeGenerator(Collection<? extends TransportRenderer> renderers) {
        Map<TransportType, TransportRenderer> registered = new LinkedHashMap<>();
        for (TransportRenderer renderer : renderers) {
            TransportRenderer previous = registered.putIfAbsent(renderer.transport(), renderer);
            if (previous != null) throw new IllegalArgumentException("Duplicate transport renderer: " + renderer.transport().bindingName());
        }
        this.transportRenderers = Map.copyOf(registered);
    }

    public List<GeneratedSource> generate(GenerationRequest request) throws IOException {
        JsonNode root = mapper.readTree(Files.newBufferedReader(request.specification()));
        if (!root.path("asyncapi").asText().startsWith("3.")) {
            throw new IllegalArgumentException("Only AsyncAPI 3.x documents are supported");
        }
        AsyncApiDocument document = parse(root);
        List<GeneratedSource> sources = new ArrayList<>();
        GenerationOptions options = request.options();
        validateSchemaFormats(document);
        if (options.generateModels()) {
            for (SchemaRenderer renderer : List.of(new JsonSchemaModelRenderer(), new AvroModelRenderer())) {
                sources.addAll(renderer.render(document.schemas(), options));
            }
        }
        if (options.generateContracts()) sources.addAll(new ContractRenderer().render(document, options.contractPackage(), options.modelPackage()));
        for (TransportRenderer renderer : renderers(document)) {
            sources.addAll(renderer.render(document, options));
        }
        write(sources, request.outputDirectory());
        return List.copyOf(sources);
    }

    private List<TransportRenderer> renderers(AsyncApiDocument document) {
        Map<String, TransportRenderer> selected = new LinkedHashMap<>();
        for (AsyncApiDocument.Operation operation : document.operations()) {
            for (String binding : operation.transports()) {
                TransportType transport = TransportType.fromBinding(binding).orElse(null);
                TransportRenderer renderer = transport == null ? null : transportRenderers.get(transport);
                if (renderer == null) {
                    throw new IllegalArgumentException("Operation " + operation.name()
                        + " uses unsupported transport binding: " + binding);
                }
                selected.putIfAbsent(binding, renderer);
            }
        }
        return List.copyOf(selected.values());
    }

    private void validateSchemaFormats(AsyncApiDocument document) {
        for (AsyncApiDocument.Operation operation : document.operations()) {
            if (operation.payloadFormat() == SchemaFormat.PROTOBUF) {
                throw new IllegalArgumentException("Operation " + operation.name()
                    + " uses Protobuf. Add a Protobuf SchemaRenderer/protoc integration before generating this contract.");
            }
            if (operation.payloadFormat() == SchemaFormat.UNKNOWN) {
                throw new IllegalArgumentException("Operation " + operation.name() + " uses an unknown payload schema format");
            }
        }
    }

    private AsyncApiDocument parse(JsonNode root) {
        Map<String, JsonNode> schemas = new LinkedHashMap<>();
        root.path("components").path("schemas").fields().forEachRemaining(e -> schemas.put(e.getKey(), e.getValue()));
        List<AsyncApiDocument.Operation> operations = new ArrayList<>();
        root.path("operations").fields().forEachRemaining(e -> {
            JsonNode operation = resolve(root, e.getValue());
            operations.add(parseOperation(root, e.getKey(), operation));
        });
        return new AsyncApiDocument(Map.copyOf(schemas), List.copyOf(operations));
    }

    private AsyncApiDocument.Operation parseOperation(JsonNode root, String name, JsonNode raw) {
        JsonNode operation = resolve(root, raw);
        Action action = "send".equals(operation.path("action").asText()) ? Action.SEND : Action.RECEIVE;
        JsonNode channel = resolve(root, operation.path("channel"));
        String topic = channel.path("address").asText();
        if (topic.isBlank()) throw new IllegalArgumentException("Operation " + name + " has no channel address");
        JsonNode messages = operation.path("messages");
        JsonNode rawMessage = messages.isArray() && !messages.isEmpty() ? messages.get(0) : first(channel.path("messages"));
        JsonNode message = resolve(root, rawMessage);
        if (message.isMissingNode() || message.isNull()) throw new IllegalArgumentException("Operation " + name + " has no message");
        Set<String> transports = new java.util.LinkedHashSet<>();
        bindingNames(channel, transports);
        bindingNames(operation, transports);
        bindingNames(message, transports);
        if (transports.isEmpty()) {
            throw new IllegalArgumentException("Operation " + name + " has no transport binding");
        }
        // Keep payload/header references intact: renderers need their declared Java type,
        // while the parser has already resolved the message and channel topology.
        JsonNode payload = message.path("payload");
        return new AsyncApiDocument.Operation(name, action, topic, javaName(name), payload,
            resolve(root, message.path("headers")), Set.copyOf(transports),
            SchemaFormat.from(resolve(root, payload).path("schemaFormat").asText()));
    }

    private JsonNode first(JsonNode node) {
        if (node.isObject()) { Iterator<JsonNode> values = node.elements(); return values.hasNext() ? values.next() : node; }
        return node;
    }

    private void bindingNames(JsonNode source, Set<String> transports) {
        source.path("bindings").fieldNames().forEachRemaining(transports::add);
    }

    static JsonNode resolve(JsonNode root, JsonNode node) {
        JsonNode current = node;
        while (current != null && current.has("$ref")) {
            String ref = current.path("$ref").asText();
            if (!ref.startsWith("#/")) throw new IllegalArgumentException("External references are not supported: " + ref);
            current = root.at(ref.substring(1));
            if (current.isMissingNode()) throw new IllegalArgumentException("Unresolved reference: " + ref);
        }
        return current == null ? MissingNode.getInstance() : current;
    }

    static String javaName(String value) {
        StringBuilder result = new StringBuilder();
        for (String part : value.replaceAll("[^A-Za-z0-9]+", " ").trim().split("\\s+")) {
            if (!part.isEmpty()) result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
        }
        return result.isEmpty() ? "GeneratedType" : result.toString();
    }

    private void write(List<GeneratedSource> sources, Path root) throws IOException {
        for (GeneratedSource source : sources) {
            Path file = root.resolve(source.relativePath());
            Files.createDirectories(file.getParent());
            Files.writeString(file, source.content());
        }
    }
}
