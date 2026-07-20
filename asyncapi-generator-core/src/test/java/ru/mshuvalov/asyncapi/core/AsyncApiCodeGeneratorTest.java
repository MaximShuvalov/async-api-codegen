package ru.mshuvalov.asyncapi.core;

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
        String model = Files.readString(output.resolve("example/generated/model/Order.java"));
        assertTrue(model.contains("byte[] raw"));
        assertTrue(model.contains("byte[] encoded"));
    }
}
