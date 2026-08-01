package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.AsyncApiDocument;
import ru.mshuvalov.asyncapi.core.model.TransportType;
import ru.mshuvalov.asyncapi.core.sourcegenerator.KafkaConsumerSourceGeneratorInputData;
import ru.mshuvalov.asyncapi.core.sourcegenerator.KafkaProducerSourceGeneratorInputData;
import ru.mshuvalov.asyncapi.core.sourcegenerator.SourceGenerator;

import java.util.ArrayList;
import java.util.List;

final class KafkaTransportRenderer implements TransportRenderer {

    private final SourceGenerator sourceGenerator;

    public KafkaTransportRenderer(SourceGenerator sourceGenerator) {
        this.sourceGenerator = sourceGenerator;
    }

    @Override
    public TransportType transport() {
        return TransportType.KAFKA;
    }

    @Override
    public List<GeneratedSource> render(AsyncApiDocument document, GenerationOptions options) {
        List<GeneratedSource> sources = new ArrayList<>(new ContractRenderer().render(document, options));
        for (AsyncApiDocument.Operation op : document.operations()) {
            if (!op.transports().contains(transport().bindingName())) {
                continue;
            }
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
        String className = operation.messageName() + "KafkaProducer";
        var generatorData = new KafkaProducerSourceGeneratorInputData(
                className,
                operation.messageName(),
                payload,
                headers,
                ContractRenderer.channelConstant(operation.topic()),
                options.kafkaPackage(),
                (ContractRenderer.requiresModelImport(operation.payload()))
        );

        return new GeneratedSource(path(options.kafkaPackage(), className), sourceGenerator.generate(generatorData));
    }

    private GeneratedSource renderListenerAdapter(AsyncApiDocument.Operation operation, GenerationOptions options, String payload, String headers) {
        String className = operation.messageName() + "KafkaListenerAdapter";
        var generatorData = new KafkaConsumerSourceGeneratorInputData(
                className,
                operation.messageName(),
                payload,
                ContractRenderer.channelConstant(operation.topic()),
                options.kafkaPackage(),
                (ContractRenderer.requiresModelImport(operation.payload()))
        );

        return new GeneratedSource(path(options.kafkaPackage(), className), sourceGenerator.generate(generatorData));
    }

    private String path(String packageName, String name) {
        return packageName.replace('.', '/') + "/" + name + ".java";
    }
}
