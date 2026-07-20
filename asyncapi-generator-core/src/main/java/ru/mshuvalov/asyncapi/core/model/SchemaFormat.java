package ru.mshuvalov.asyncapi.core.model;

/** Schema language declared by AsyncAPI's Multi Format Schema Object. */
public enum SchemaFormat {
    ASYNCAPI_SCHEMA,
    JSON_SCHEMA,
    AVRO,
    PROTOBUF,
    UNKNOWN;

    public static SchemaFormat from(String value) {
        if (value == null || value.isBlank() || value.contains("vnd.aai.asyncapi") || value.contains("application/schema")) return ASYNCAPI_SCHEMA;
        String normalized = value.toLowerCase(java.util.Locale.ROOT);
        if (normalized.contains("avro")) return AVRO;
        if (normalized.contains("protobuf")) return PROTOBUF;
        return UNKNOWN;
    }
}
