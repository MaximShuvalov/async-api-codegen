package ru.mshuvalov.asyncapi.core;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AsyncApiCodeGeneratorTest {
    @Test void generatesModelsContractsAndKafkaProducer() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels: { orders: { address: orders.created, bindings: { kafka: {} } } }
            operations:
              sendOrder: { action: send, channel: { $ref: '#/channels/orders' }, messages: [{ payload: { $ref: '#/components/schemas/Order' } }] }
            components:
              schemas:
                Order: { type: object, properties: { id: { type: string }, raw: { type: string, format: byte }, encoded: { type: string, contentEncoding: base64 } } }
            """);
        Path output = Files.createTempDirectory("generated");
        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, GenerationOptions.defaults("example.generated")));
        assertTrue(Files.exists(output.resolve("example/generated/kafka/dto/Order.java")));
        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderKafkaProducer.java")));
        assertTrue(Files.readString(output.resolve("example/generated/kafka/Channels.java")).contains("ORDERS"));
        assertFalse(Files.exists(output.resolve("example/generated/contract")));
        String model = Files.readString(output.resolve("example/generated/kafka/dto/Order.java"));
        assertTrue(model.contains("byte[] raw"));
        assertTrue(model.contains("byte[] encoded"));
    }

    @Test void ignoresOperationsReferencedFromExternalFiles() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels: { orders: { address: orders.created, bindings: { kafka: {} } } }
            operations:
              externalSend: { $ref: 'shared-operations.yaml#/sendOrder' }
              sendOrder: { action: send, channel: { $ref: '#/channels/orders' }, messages: [{ payload: {} }] }
            """);
        Path output = Files.createTempDirectory("generated");

        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, GenerationOptions.defaults("example.generated")));

        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderKafkaProducer.java")));
        assertFalse(Files.exists(output.resolve("example/generated/kafka/ExternalSendKafkaProducer.java")));
    }

    @Test void usesLocalMessageFieldsAlongsideAnExternalReference() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels:
              orders: { address: orders.created, bindings: { kafka: {} } }
            operations:
              sendOrder:
                action: send
                channel: { $ref: '#/channels/orders' }
                messages: [{ $ref: '#/components/messages/OrderCreated' }]
            components:
              messages:
                OrderCreated:
                  $ref: https://example.test/common-messages.yaml#/OrderCreated
                  name: OrderCreated
                  payload: { type: object, properties: { id: { type: string } } }
            """);
        Path output = Files.createTempDirectory("generated");

        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, GenerationOptions.defaults("example.generated")));

        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderKafkaProducer.java")));
        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderPublisher.java")));
    }

    @Test void generatesBinaryInlinePayloadWithoutAModelImport() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels:
              files: { address: files.created, bindings: { kafka: {} } }
            operations:
              sendFile:
                action: send
                channel: { $ref: '#/channels/files' }
                messages: [{ $ref: '#/components/messages/FileCreated' }]
              receiveFile:
                action: receive
                channel: { $ref: '#/channels/files' }
                messages: [{ $ref: '#/components/messages/FileCreated' }]
            components:
              messages:
                FileCreated:
                  $ref: https://example.test/common-messages.yaml#/FileCreated
                  name: FileCreated
                  payload: { type: string, format: binary }
            """);
        Path output = Files.createTempDirectory("generated");

        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, GenerationOptions.defaults("example.generated")));

        String contract = Files.readString(output.resolve("example/generated/kafka/SendFilePublisher.java"));
        String producer = Files.readString(output.resolve("example/generated/kafka/SendFileKafkaProducer.java"));
        String listener = Files.readString(output.resolve("example/generated/kafka/ReceiveFileKafkaListenerAdapter.java"));
        assertTrue(contract.contains("void send(byte[] payload"));
        assertFalse(contract.contains("example.generated.kafka.dto"));
        assertTrue(producer.contains("KafkaTemplate<String, byte[]>"));
        assertFalse(producer.contains("example.generated.kafka.dto"));
        assertTrue(listener.contains("void onMessage(byte[] payload)"));
        assertFalse(listener.contains("example.generated.kafka.dto"));
    }

    @Test void generatesOnlyEnabledKafkaDirections() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels: { events: { address: events, bindings: { kafka: {} } } }
            operations:
              sendEvent: { action: send, channel: { $ref: '#/channels/events' }, messages: [{ payload: {} }] }
              receiveEvent: { action: receive, channel: { $ref: '#/channels/events' }, messages: [{ payload: {} }] }
            """);
        Path producerOutput = Files.createTempDirectory("generated-producer");
        Path consumerOutput = Files.createTempDirectory("generated-consumer");

        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, producerOutput,
            new GenerationOptions("example.kafka", true, true, false)));
        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, consumerOutput,
            new GenerationOptions("example.kafka", true, false, true)));

        assertTrue(Files.exists(producerOutput.resolve("example/kafka/SendEventPublisher.java")));
        assertTrue(Files.exists(producerOutput.resolve("example/kafka/SendEventHeaders.java")));
        assertTrue(Files.exists(producerOutput.resolve("example/kafka/SendEventKafkaProducer.java")));
        assertFalse(Files.exists(producerOutput.resolve("example/kafka/ReceiveEventHandler.java")));
        assertFalse(Files.exists(producerOutput.resolve("example/kafka/ReceiveEventHeaders.java")));
        assertFalse(Files.exists(producerOutput.resolve("example/kafka/ReceiveEventKafkaListenerAdapter.java")));
        assertTrue(Files.exists(consumerOutput.resolve("example/kafka/ReceiveEventHandler.java")));
        assertTrue(Files.exists(consumerOutput.resolve("example/kafka/ReceiveEventHeaders.java")));
        assertTrue(Files.exists(consumerOutput.resolve("example/kafka/ReceiveEventKafkaListenerAdapter.java")));
        assertFalse(Files.exists(consumerOutput.resolve("example/kafka/SendEventPublisher.java")));
        assertFalse(Files.exists(consumerOutput.resolve("example/kafka/SendEventHeaders.java")));
        assertFalse(Files.exists(consumerOutput.resolve("example/kafka/SendEventKafkaProducer.java")));
    }
}
