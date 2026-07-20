package ru.mshuvalov.asyncapi.core;

import static org.junit.jupiter.api.Assertions.assertTrue;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class AvroModelRendererTest {
    @Test void generatesRecordFromAvroMultiFormatSchema() throws Exception {
        Path spec = Files.createTempFile("asyncapi", ".yaml");
        Files.writeString(spec, """
            asyncapi: 3.0.0
            channels: { users: { address: users, bindings: { kafka: {} } } }
            operations:
              sendUser: { action: send, channel: { $ref: '#/channels/users' }, messages: [{ payload: { $ref: '#/components/schemas/User' } }] }
            components:
              schemas:
                User:
                  schemaFormat: application/vnd.apache.avro+json;version=1.9.0
                  schema:
                    type: record
                    name: User
                    fields:
                      - { name: id, type: string }
                      - { name: content, type: bytes }
            """);
        Path output = Files.createTempDirectory("generated");
        new AsyncApiCodeGenerator().generate(new GenerationRequest(spec, output, GenerationOptions.defaults("example")));
        String model = Files.readString(output.resolve("example/model/User.java"));
        assertTrue(model.contains("String id"));
        assertTrue(model.contains("byte[] content"));
    }
}
