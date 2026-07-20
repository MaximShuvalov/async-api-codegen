package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import ru.mshuvalov.asyncapi.core.model.TransportType;

import java.util.ArrayList;
import java.util.List;

final class KafkaTransportRenderer implements TransportRenderer {
    @Override public TransportType transport() { return TransportType.KAFKA; }

    @Override public List<GeneratedSource> render(AsyncApiDocument document, GenerationOptions options) {
        List<GeneratedSource> sources = new ArrayList<>();
        sources.addAll(new ContractRenderer().render(document, options));
        for (AsyncApiDocument.Operation op : document.operations()) {
            if (!op.transports().contains(transport().bindingName())) continue;
            String payload = ContractRenderer.payloadType(op.payload());
            String headers = op.messageName() + "Headers";
            if (op.action() == AsyncApiDocument.Action.SEND && options.generateProducers()) {
                sources.add(renderProducer(op, options, payload, headers));
            } else if (op.action() == AsyncApiDocument.Action.RECEIVE && options.generateConsumers()) {
                sources.add(renderListenerAdapter(op, options, payload, headers));
            }
        }
        return sources;
    }

    private GeneratedSource renderProducer(AsyncApiDocument.Operation operation, GenerationOptions options, String payload, String headers) {
        String name = operation.messageName() + "KafkaProducer";
        String content = "package " + options.kafkaPackage() + ";\n\n"
            + (ContractRenderer.requiresModelImport(operation.payload()) ? "import " + options.dtoPackage() + "." + payload + ";\n" : "")
            + "import org.springframework.kafka.core.KafkaTemplate;\n"
            + "import org.springframework.stereotype.Component;\n\n"
            + "@Component\npublic class " + name + " implements " + operation.messageName() + "Publisher {\n"
            + "    private final KafkaTemplate<String, " + payload + "> kafkaTemplate;\n"
            + "    public " + name + "(KafkaTemplate<String, " + payload + "> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }\n"
            + "    @Override public void send(" + payload + " payload, " + headers + " headers) { kafkaTemplate.send(Channels."
            + ContractRenderer.channelConstant(operation.topic()) + ", payload); }\n}\n";
        return new GeneratedSource(path(options.kafkaPackage(), name), content);
    }

    private GeneratedSource renderListenerAdapter(AsyncApiDocument.Operation operation, GenerationOptions options, String payload, String headers) {
        String name = operation.messageName() + "KafkaListenerAdapter";
        String channel = ContractRenderer.channelConstant(operation.topic());
        String content = "package " + options.kafkaPackage() + ";\n\n"
            + (ContractRenderer.requiresModelImport(operation.payload()) ? "import " + options.dtoPackage() + "." + payload + ";\n" : "")
            + "import org.springframework.kafka.annotation.KafkaListener;\n"
            + "import org.springframework.stereotype.Component;\n\n"
            + "@Component\npublic class " + name + " {\n    private final " + operation.messageName() + "Handler handler;\n"
            + "    public " + name + "(" + operation.messageName() + "Handler handler) { this.handler = handler; }\n"
            + "    @KafkaListener(topics = Channels." + channel + ")\n"
            + "    public void onMessage(" + payload + " payload) { handler.handle(payload, null, new MessageMetadata(Channels."
            + channel + ", null)); }\n}\n";
        return new GeneratedSource(path(options.kafkaPackage(), name), content);
    }

    private String path(String packageName, String name) { return packageName.replace('.', '/') + "/" + name + ".java"; }
}
