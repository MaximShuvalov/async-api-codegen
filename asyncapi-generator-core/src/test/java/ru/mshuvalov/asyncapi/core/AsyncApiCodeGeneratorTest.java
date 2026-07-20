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
        assertTrue(Files.exists(output.resolve("example/generated/model/Order.java")));
        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderKafkaProducer.java")));
        assertTrue(Files.readString(output.resolve("example/generated/contract/Channels.java")).contains("ORDERS"));
        String model = Files.readString(output.resolve("example/generated/model/Order.java"));
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
        assertTrue(Files.exists(output.resolve("example/generated/contract/SendOrderPublisher.java")));
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

        String contract = Files.readString(output.resolve("example/generated/contract/SendFilePublisher.java"));
        String producer = Files.readString(output.resolve("example/generated/kafka/SendFileKafkaProducer.java"));
        String listener = Files.readString(output.resolve("example/generated/kafka/ReceiveFileKafkaListenerAdapter.java"));
        assertTrue(contract.contains("void send(byte[] payload"));
        assertFalse(contract.contains("example.generated.model"));
        assertTrue(producer.contains("KafkaTemplate<String, byte[]>"));
        assertFalse(producer.contains("example.generated.model"));
        assertTrue(listener.contains("void onMessage(byte[] payload)"));
        assertFalse(listener.contains("example.generated.model"));
    }
}
