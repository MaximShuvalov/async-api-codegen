# Генератор кода AsyncAPI для Spring

Генератор кода на основе AsyncAPI

## Модули

* `asyncapi-generator-core` — независимые от Gradle парсер, внутренняя модель и Java-рендереры.
* `asyncapi-gradle-plugin` — плагин `ru.mshuvalov.asyncapi` и кэшируемая задача `generateAsyncApi`.
* `samples/spring-kafka` — рабочая конфигурация Kafka/Spring Boot.

## Использование

```groovy
plugins { id 'ru.mshuvalov.asyncapi.codegen' version '0.1.0' }

asyncApiSpring {
  specification = layout.projectDirectory.file('src/main/asyncapi/api.yaml')
  basePackage = 'com.example.generated'
  generateModel(true)
  generateProducer(true)
  generateConsumer(false)
  kafkaPackage('com.example.infrastructure.kafka')
}
```

Задача записывает файлы только в `build/generated/sources/asyncapi/java/main`; этот каталог автоматически добавляется к исходникам `main`. Транспорт определяется для каждой операции исключительно по стандартным AsyncAPI `bindings` её канала, операции или сообщения. В MVP поддерживается `kafka`; операция с неподдерживаемым binding завершается ошибкой с именем операции и значением binding. Операции `send` создают Kafka publisher и продюсеры, а `receive` — Kafka handler и адаптеры listener.

Параметры `generateModel`, `generateProducer` и `generateConsumer` определяют, какие категории артефактов создавать. Все Kafka-артефакты находятся под `<basePackage>.kafka`: payload-схемы — в `.kafka.dto`, а `Channels`, metadata, headers, publisher/handler, producer и listener — непосредственно в `.kafka`. При отключённом producer или consumer не создаются соответствующие `send`- или `receive`-артефакты. Отдельные общие пакеты моделей или контрактов не создаются.

Плагин также поддерживает Spring-проекты на Kotlin/JVM: он генерирует Java-типы в обычный набор исходников Java `main` и делает `compileKotlin` зависимой от `generateAsyncApi`.

## Поддержка и ограничения MVP

Поддерживаются операции и каналы AsyncAPI 3.0, встроенные и компонентные сообщения, `$ref`, JSON Schema для объектов, массивов, карт и примитивов, enum, допускающие `null` поля, типизированные заголовки и Kafka topic bindings. Строки JSON Schema с `format: byte` или `binary`, а также с `contentEncoding: base64`, генерируются как `byte[]`. Намеренно не создаются аннотации валидации, полиморфные схемы (`oneOf`/`allOf`) и пользовательские обработчики бизнес-логики. Поддержка AsyncAPI 2 предусмотрена для будущего адаптера к той же внутренней модели.
