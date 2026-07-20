package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.SchemaFormat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

class SchemaFormatTest {
    @Test void distinguishesExplicitJsonSchemaFromAsyncApiDefault() {
        assertEquals(SchemaFormat.ASYNCAPI_SCHEMA, SchemaFormat.from(""));
        assertEquals(SchemaFormat.JSON_SCHEMA, SchemaFormat.from("application/schema+json;version=draft-07"));
    }
}
