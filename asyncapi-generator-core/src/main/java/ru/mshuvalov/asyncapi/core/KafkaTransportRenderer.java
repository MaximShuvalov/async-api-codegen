package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;

final class KafkaTransportRenderer implements TransportRenderer {
    @Override public TransportType transport() { return TransportType.KAFKA; }

    @Override public List<GeneratedSource> render(AsyncApiDocument document, GenerationOptions options) {
        List<GeneratedSource> sources = new ArrayList<>();
        for (AsyncApiDocument.Operation op : document.operations()) {
            if (!op.transports().contains(transport().bindingName())) continue;
            String payload = ContractRenderer.payloadType(op.payload());
            String headers = op.messageName() + "Headers";
            if (op.action() == AsyncApiDocument.Action.SEND && options.generateProducers()) {
                String name = op.messageName() + "KafkaProducer";
                String content = "package " + options.kafkaPackage() + ";\n\n"
                    + "import " + options.contractPackage() + "." + op.messageName() + "Publisher;\n"
                    + "import " + options.contractPackage() + "." + headers + ";\n"
                    + "import " + options.contractPackage() + ".Channels;\n"
                    + "import " + options.modelPackage() + "." + payload + ";\n"
                    + "import org.springframework.kafka.core.KafkaTemplate;\n"
                    + "import org.springframework.stereotype.Component;\n\n"
                    + "@Component\npublic class " + name + " implements " + op.messageName() + "Publisher {\n"
                    + "    private final KafkaTemplate<String, " + payload + "> kafkaTemplate;\n"
                    + "    public " + name + "(KafkaTemplate<String, " + payload + "> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }\n"
                    + "    @Override public void send(" + payload + " payload, " + headers + " headers) { kafkaTemplate.send(Channels." + ContractRenderer.channelConstant(op.topic()) + ", payload); }\n}\n";
                sources.add(new GeneratedSource(path(options.kafkaPackage(), name), content));
            } else if (op.action() == AsyncApiDocument.Action.RECEIVE && options.generateConsumers()) {
                String name = op.messageName() + "KafkaListenerAdapter";
                String content = "package " + options.kafkaPackage() + ";\n\n"
                    + "import " + options.contractPackage() + "." + op.messageName() + "Handler;\n"
                    + "import " + options.contractPackage() + "." + headers + ";\n"
                    + "import " + options.contractPackage() + ".MessageMetadata;\n"
                    + "import " + options.contractPackage() + ".Channels;\n"
                    + "import " + options.modelPackage() + "." + payload + ";\n"
                    + "import org.springframework.kafka.annotation.KafkaListener;\n"
                    + "import org.springframework.stereotype.Component;\n\n"
                    + "@Component\npublic class " + name + " {\n    private final " + op.messageName() + "Handler handler;\n"
                    + "    public " + name + "(" + op.messageName() + "Handler handler) { this.handler = handler; }\n"
                    + "    @KafkaListener(topics = Channels." + ContractRenderer.channelConstant(op.topic()) + ")\n"
                    + "    public void onMessage(" + payload + " payload) { handler.handle(payload, null, new MessageMetadata(Channels." + ContractRenderer.channelConstant(op.topic()) + ", null)); }\n}\n";
                sources.add(new GeneratedSource(path(options.kafkaPackage(), name), content));
            }
        }
        return sources;
    }
    private String path(String packageName, String name) { return packageName.replace('.', '/') + "/" + name + ".java"; }
}
