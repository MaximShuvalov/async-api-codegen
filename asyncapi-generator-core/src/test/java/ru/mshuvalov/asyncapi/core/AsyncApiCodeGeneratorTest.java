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
            channels: { orders: { address: orders.created } }
            operations:
              sendOrder: { action: send, channel: { $ref: '#/channels/orders' }, messages: [{ payload: { $ref: '#/components/schemas/Order' } }] }
            components:
              schemas:
                Order: { type: object, properties: { id: { type: string } } }
            """);
        Path output = Files.createTempDirectory("generated");
        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, "example.generated", "kafka"));
        assertTrue(Files.exists(output.resolve("example/generated/model/Order.java")));
        assertTrue(Files.exists(output.resolve("example/generated/kafka/SendOrderKafkaProducer.java")));
    }
}
