# Генератор кода AsyncAPI для Spring

Многомодульный Gradle-проект, генерирующий Java 21-код для обмена сообщениями в Spring на основе документов AsyncAPI 3.0.

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
  modelPackage('com.example.contract.model')
  contractPackage('com.example.contract.api')
  kafkaPackage('com.example.infrastructure.kafka')
}
```

Задача записывает файлы только в `build/generated/sources/asyncapi/java/main`; этот каталог автоматически добавляется к исходникам `main`. Транспорт определяется для каждой операции исключительно по стандартным AsyncAPI `bindings` её канала, операции или сообщения. В MVP поддерживается `kafka`; операция с неподдерживаемым binding завершается ошибкой с именем операции и значением binding. Операции `send` создают контракты издателя и Kafka-продюсеры, а `receive` — контракты обработчика и адаптеры listener.

Параметры `generateModel`, `generateContract`, `generateProducer` и `generateConsumer` определяют, какие категории артефактов создавать. При генерации продюсера или consumer контракты должны оставаться включёнными. По умолчанию используются пакеты `<basePackage>.model`, `.contract` и `.kafka`.

Плагин также поддерживает Spring-проекты на Kotlin/JVM: он генерирует Java-типы в обычный набор исходников Java `main` и делает `compileKotlin` зависимой от `generateAsyncApi`.

## Поддержка и ограничения MVP

Поддерживаются операции и каналы AsyncAPI 3.0, встроенные и компонентные сообщения, `$ref`, JSON Schema для объектов, массивов, карт и примитивов, enum, допускающие `null` поля, типизированные заголовки и Kafka topic bindings. Строки JSON Schema с `format: byte` или `binary`, а также с `contentEncoding: base64`, генерируются как `byte[]`. Намеренно не создаются аннотации валидации, полиморфные схемы (`oneOf`/`allOf`) и пользовательские обработчики бизнес-логики. Поддержка AsyncAPI 2 предусмотрена для будущего адаптера к той же внутренней модели.
