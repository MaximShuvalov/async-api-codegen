# AsyncAPI Spring Code Generator

Gradle-плагин, который генерирует Java код для Spring Kafka из документа AsyncAPI 3.0. Генерация выполняется задачей `generateAsyncApi` и записывается в `build/generated/sources/asyncapi/java/main`; каталог автоматически подключается к исходникам `main`.

Проект состоит из:

* `asyncapi-generator-core` — парсинг AsyncAPI и рендеринг исходников;
* `asyncapi-gradle-plugin` — Gradle-плагин `ru.mshuvalov.asyncapi.codegen`;
* `samples/spring-kafka` — пример Spring Boot/Kafka-проекта.

## Подключение

```groovy
plugins {
  id 'ru.mshuvalov.asyncapi.codegen' version '0.1.0'
}

asyncApiSpring {
  specification = layout.projectDirectory.file('src/main/asyncapi/orders.yaml')
  basePackage = 'com.example.orders.generated'

  // Необязательные настройки.
  kafkaPackage('com.example.orders.messaging.kafka')
  generateModel(true)
  generateProducer(true)
  generateConsumer(true)
}
```

По умолчанию `basePackage` равен `generated.asyncapi`, `specification` — `src/main/asyncapi/asyncapi.yaml`, а `kafkaPackage` — `<basePackage>.kafka`.

Плагин подключает `generateAsyncApi` к `compileJava`. В Kotlin/JVM-проектах `compileKotlin` также зависит от этой задачи.

## Структура сгенерированного кода

Генератор не создаёт общий слой `contract` или `model`. Все типы, относящиеся к транспорту, находятся под `kafkaPackage`:

```text
com.example.orders.messaging.kafka
├── Channels.java
├── MessageMetadata.java
├── PublishOrderPublisher.java
├── PublishOrderHeaders.java
├── PublishOrderKafkaProducer.java
├── ConsumeOrderHandler.java
├── ConsumeOrderHeaders.java
├── ConsumeOrderKafkaListenerAdapter.java
└── dto/
    └── OrderPayload.java
```

`dto` содержит схемы payload. В корневом пакете Kafka размещаются константы каналов, headers, publisher/handler и Spring Kafka adapter-ы.

Флаги управления генерацией:

* `generateModel(boolean)` — генерировать payload DTO;
* `generateProducer(boolean)` — генерировать только артефакты операций `send`: publisher, headers и Kafka producer;
* `generateConsumer(boolean)` — генерировать только артефакты операций `receive`: handler, headers и Kafka listener adapter.

Если направление отключено, его operation-specific типы не создаются. `Channels` и `MessageMetadata` создаются, когда включено хотя бы одно направление.

## Поддерживаемый AsyncAPI

Поддерживается AsyncAPI 3.0: операции, каналы, встроенные и компонентные сообщения, локальные `$ref`, JSON Schema для объектов, массивов, map и примитивов, enum, nullable-поля, типизированные headers и Kafka bindings на канале, операции или сообщении.

Транспорт определяется по стандартному `bindings`. Сейчас поддерживается только `kafka`; операция с другим binding завершается ошибкой.

Для строковых схем `format: byte`, `format: binary` и `contentEncoding: base64` генерируется `byte[]`, в том числе для inline payload. Поддерживаются component Avro record/enum-схемы.

## Ограничения

Не поддерживаются AsyncAPI 2.x, `oneOf`/`allOf`, аннотации валидации, пользовательская бизнес-логика и Protobuf без отдельного `SchemaRenderer`/интеграции с `protoc`.
