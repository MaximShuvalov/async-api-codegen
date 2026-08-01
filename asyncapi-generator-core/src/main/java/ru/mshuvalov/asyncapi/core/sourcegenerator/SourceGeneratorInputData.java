package ru.mshuvalov.asyncapi.core.sourcegenerator;

public sealed interface SourceGeneratorInputData permits KafkaProducerSourceGeneratorInputData,
        KafkaConsumerSourceGeneratorInputData {
}
