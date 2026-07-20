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
  generateModel(true)
  generateProducer(true)
  generateConsumer(false)
  modelPackage('com.example.contract.model')
  contractPackage('com.example.contract.api')
  kafkaPackage('com.example.infrastructure.kafka')
}
```

The task writes only to `build/generated/sources/asyncapi/java/main`; this directory is added to `main` sources automatically. The transport is inferred per operation only from standard AsyncAPI `bindings` on its channel, operation, or message. The MVP recognizes `kafka`; an operation with an unsupported binding fails with its name and binding value. `send` operations yield publisher contracts and Kafka producers; `receive` operations yield handler contracts and listener adapters.

`generateModel`, `generateContract`, `generateProducer`, and `generateConsumer` control artifact categories. Contracts must remain enabled when a producer or consumer is generated. By default packages are `<basePackage>.model`, `.contract`, and `.kafka`.

## MVP support and limits

Supports AsyncAPI 3.0 operations, channels, inline/component messages, `$ref`, object/array/map/primitive JSON Schema, enums, nullable fields, typed headers and Kafka topic bindings. JSON Schema strings with `format: byte` or `binary`, or `contentEncoding: base64`, are generated as `byte[]`. It deliberately does not generate validation annotations, polymorphic schemas (`oneOf`/`allOf`) or user business handlers. AsyncAPI 2 support is reserved for a future adapter to the same internal model.
