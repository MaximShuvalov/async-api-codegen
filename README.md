# AsyncAPI Spring code generator

Multi-module Gradle project that generates Java 21/Spring messaging code from AsyncAPI 3.0 documents.

## Modules

* `asyncapi-generator-core` — Gradle-independent parser, internal model and Java renderers.
* `asyncapi-gradle-plugin` — `ru.mshuvalov.asyncapi` plugin and cacheable `generateAsyncApi` task.
* `samples/spring-kafka` — a working Kafka/Spring Boot configuration.

## Usage

```groovy
plugins { id 'ru.mshuvalov.asyncapi' version '0.1.0' }

asyncApiSpring {
  specification = layout.projectDirectory.file('src/main/asyncapi/api.yaml')
  basePackage = 'com.example.generated'
}
```

The task writes only to `build/generated/sources/asyncapi/java/main`; this directory is added to `main` sources automatically. The transport is inferred only from standard AsyncAPI `bindings` on a channel, operation, or message (the MVP recognizes `kafka`). `send` operations yield publisher contracts and Kafka producers; `receive` operations yield handler contracts and listener adapters.

## MVP support and limits

Supports AsyncAPI 3.0 operations, channels, inline/component messages, `$ref`, object/array/map/primitive JSON Schema, enums, nullable fields, typed headers and Kafka topic bindings. It deliberately does not generate validation annotations, polymorphic schemas (`oneOf`/`allOf`) or user business handlers. AsyncAPI 2 support is reserved for a future adapter to the same internal model.
