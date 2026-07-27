# AsyncAPI Spring Code Generator

`asyncapi-spring-codegen` — Gradle-плагин и библиотека для генерации Java/Spring messaging-кода по документам AsyncAPI 3.0. Текущая транспортная реализация — Spring Kafka.

Г енерация выполняется задачей `generateAsyncApi`. Сгенерированные исходники помещаются в `build/generated/sources/asyncapi/java/main` и автоматически добавляются в исходники `main` Java-проекта.

## Возможности

Проект состоит из двух модулей и примера:

* `asyncapi-generator-core` — парсинг AsyncAPI и рендеринг исходников;
* `asyncapi-gradle-plugin` — Gradle-плагин `ru.mshuvalov.asyncapi.codegen`;
* `samples/spring-kafka` — минимальный Spring Boot/Kafka-проект с примером AsyncAPI-документа.

Генератор создаёт:

* payload DTO из схем;
* типы для Kafka-каналов и metadata;
* publisher и producer для операций отправки (`send`);
* handler и listener adapter для операций получения (`receive`).

## Требования

* JDK 21 — оба модуля проекта используют Java toolchain 21;
* Gradle Wrapper из репозитория (`./gradlew`).

## Подключение к Gradle-проекту

Подключите плагин и настройте расширение `asyncApiSpring`:

```groovy
plugins {
  id 'ru.mshuvalov.asyncapi.codegen' version '<версия-плагина>'
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

Укажите вместо `<версия-плагина>` версию, доступную в репозитории зависимостей проекта. Значения по умолчанию:

* `specification` — `src/main/asyncapi/asyncapi.yaml`;
* `basePackage` — `generated.asyncapi`;
* `kafkaPackage` — `<basePackage>.kafka`;
* `generateModel`, `generateProducer`, `generateConsumer` — `true`.

После применения плагина:

```bash
./gradlew generateAsyncApi
```

Плагин связывает `generateAsyncApi` с `compileJava`. Для Kotlin/JVM-проектов `compileKotlin` также зависит от этой задачи.

## Настройки генерации

| Настройка | Назначение |
| --- | --- |
| `specification` | AsyncAPI-файл, из которого выполняется генерация. |
| `basePackage` | Базовый Java-пакет проекта. |
| `kafkaPackage` | Пакет с транспортными Kafka-типами. |
| `generateModel(boolean)` | Включает или отключает генерацию payload DTO. |
| `generateProducer(boolean)` | Включает или отключает publisher, headers и Kafka producer для операций `send`. |
| `generateConsumer(boolean)` | Включает или отключает handler, headers и Kafka listener adapter для операций `receive`. |

Если оба направления (`producer` и `consumer`) отключены, operation-specific типы не создаются. `Channels` и `MessageMetadata` создаются, если включено хотя бы одно направление.

## Структура сгенерированного кода

Генератор размещает транспортные типы в `kafkaPackage`, а схемы payload — в его пакете `dto`:

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

Имена в примере зависят от каналов, сообщений и операций из AsyncAPI-документа.

## Поддерживаемый AsyncAPI

Поддерживается AsyncAPI 3.0, включая:

* операции и каналы;
* встроенные и компонентные сообщения;
* локальные `$ref`;
* JSON Schema для объектов, массивов, map и примитивов;
* enum и nullable-поля;
* типизированные headers;
* Kafka bindings на канале, операции или сообщении;
* component Avro record/enum-схемы.

Для строковых схем с `format: byte`, `format: binary` или `contentEncoding: base64` генерируется `byte[]`, в том числе для inline payload.

Транспорт определяется по стандартному `bindings`. Поддерживается только `kafka`; операция с другим binding завершается ошибкой.

## Ограничения

Не поддерживаются:

* AsyncAPI 2.x;
* `oneOf` и `allOf`;
* аннотации валидации;
* пользовательская бизнес-логика;
* Protobuf без отдельного `SchemaRenderer` или интеграции с `protoc`.

## Пример проекта

В `samples/spring-kafka` находится Spring Boot/Kafka-проект с документом `src/main/asyncapi/orders.yaml`. Запустить генерацию для него можно из корня репозитория:

```bash
./gradlew -p samples/spring-kafka generateAsyncApi
```

Результат будет находиться в `samples/spring-kafka/build/generated/sources/asyncapi/java/main`.

## Разработка проекта

Проверка всех модулей выполняется командой:

```bash
./gradlew check
```

В `asyncapi-generator-core` и `asyncapi-gradle-plugin` тесты запускаются на JUnit Platform. Для сборки артефактов используйте:

```bash
./gradlew build
```