package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import java.util.ArrayList;
import java.util.List;

final class KafkaTransportRenderer implements TransportRenderer {
    @Override public String transport() { return "kafka"; }

    @Override public List<GeneratedSource> render(AsyncApiDocument document, String basePackage) {
        List<GeneratedSource> sources = new ArrayList<>();
        for (AsyncApiDocument.Operation op : document.operations()) {
            String payload = ContractRenderer.payloadType(op.payload());
            String headers = op.messageName() + "Headers";
            if (op.action() == AsyncApiDocument.Action.SEND) {
                String name = op.messageName() + "KafkaProducer";
                String content = "package " + basePackage + ".kafka;\n\n"
                    + "import " + basePackage + ".contract." + op.messageName() + "Publisher;\n"
                    + "import " + basePackage + ".contract." + headers + ";\n"
                    + "import " + basePackage + ".model." + payload + ";\n"
                    + "import org.springframework.kafka.core.KafkaTemplate;\n"
                    + "import org.springframework.stereotype.Component;\n\n"
                    + "@Component\npublic class " + name + " implements " + op.messageName() + "Publisher {\n"
                    + "    private final KafkaTemplate<String, " + payload + "> kafkaTemplate;\n"
                    + "    public " + name + "(KafkaTemplate<String, " + payload + "> kafkaTemplate) { this.kafkaTemplate = kafkaTemplate; }\n"
                    + "    @Override public void send(" + payload + " payload, " + headers + " headers) { kafkaTemplate.send(\"" + op.topic() + "\", payload); }\n}\n";
                sources.add(new GeneratedSource(path(basePackage, name), content));
            } else {
                String name = op.messageName() + "KafkaListenerAdapter";
                String content = "package " + basePackage + ".kafka;\n\n"
                    + "import " + basePackage + ".contract." + op.messageName() + "Handler;\n"
                    + "import " + basePackage + ".contract." + headers + ";\n"
                    + "import " + basePackage + ".contract.MessageMetadata;\n"
                    + "import " + basePackage + ".model." + payload + ";\n"
                    + "import org.springframework.kafka.annotation.KafkaListener;\n"
                    + "import org.springframework.stereotype.Component;\n\n"
                    + "@Component\npublic class " + name + " {\n    private final " + op.messageName() + "Handler handler;\n"
                    + "    public " + name + "(" + op.messageName() + "Handler handler) { this.handler = handler; }\n"
                    + "    @KafkaListener(topics = \"" + op.topic() + "\")\n"
                    + "    public void onMessage(" + payload + " payload) { handler.handle(payload, null, new MessageMetadata(\"" + op.topic() + "\", null)); }\n}\n";
                sources.add(new GeneratedSource(path(basePackage, name), content));
            }
        }
        return sources;
    }
    private String path(String base, String name) { return base.replace('.', '/') + "/kafka/" + name + ".java"; }
}
