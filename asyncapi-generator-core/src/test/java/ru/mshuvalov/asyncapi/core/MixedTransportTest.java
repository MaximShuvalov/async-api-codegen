package ru.mshuvalov.asyncapi.core;

import static org.junit.jupiter.api.Assertions.assertThrows;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class MixedTransportTest {
    @Test void identifiesUnsupportedBindingPerOperation() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels:
              kafkaChannel: { address: orders, bindings: { kafka: {} } }
              rabbitChannel: { address: payments, bindings: { amqp: {} } }
            operations:
              sendOrder: { action: send, channel: { $ref: '#/channels/kafkaChannel' }, messages: [{ payload: {} }] }
              receivePayment: { action: receive, channel: { $ref: '#/channels/rabbitChannel' }, messages: [{ payload: {} }] }
            """);
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, Files.createTempDirectory("generated"), GenerationOptions.defaults("example"))));
        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("receivePayment"));
        org.junit.jupiter.api.Assertions.assertTrue(exception.getMessage().contains("amqp"));
    }
}
