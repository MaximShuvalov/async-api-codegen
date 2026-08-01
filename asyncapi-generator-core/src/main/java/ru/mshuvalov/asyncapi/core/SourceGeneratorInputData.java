package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.MessageHandlerDirection;
import ru.mshuvalov.asyncapi.core.model.TransportType;

public sealed interface SourceGeneratorInputData permits KafkaProducerSourceGeneratorInputData, KafkaConsumerSourceGeneratorInputData {
    TransportType getTransport();
    MessageHandlerDirection getDirection();
}
