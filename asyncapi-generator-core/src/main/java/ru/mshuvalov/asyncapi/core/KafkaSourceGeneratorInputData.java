package ru.mshuvalov.asyncapi.core;

import ru.mshuvalov.asyncapi.core.model.MessageHandlerDirection;
import ru.mshuvalov.asyncapi.core.model.TransportType;

final class KafkaProducerSourceGeneratorInputData implements SourceGeneratorInputData {
    public final String className;
    public final String messageName;
    public final String payloadType;
    public final String headersType;
    public final String channelConstant;
    public final String dtoPackage;
    public final boolean requiresModelImport;

    public KafkaProducerSourceGeneratorInputData(String className, String messageName, String payloadType, String headersType,
                                                 String channelConstant, String dtoPackage, boolean requiresModelImport) {
        this.className = className;
        this.messageName = messageName;
        this.payloadType = payloadType;
        this.headersType = headersType;
        this.channelConstant = channelConstant;
        this.dtoPackage = dtoPackage;
        this.requiresModelImport = requiresModelImport;
    }

    @Override
    public TransportType getTransport() {
        return TransportType.KAFKA;
    }

    @Override
    public MessageHandlerDirection getDirection() {
        return MessageHandlerDirection.PRODUCER;
    }
}

final class KafkaConsumerSourceGeneratorInputData implements SourceGeneratorInputData {
    public final String className;
    public final String messageName;
    public final String payloadType;
    public final String channelConstant;
    public final String dtoPackage;
    public final boolean requiresModelImport;

    public KafkaConsumerSourceGeneratorInputData(String className, String messageName, String payloadType,
                                                 String channelConstant, String dtoPackage, boolean requiresModelImport) {
        this.className = className;
        this.messageName = messageName;
        this.payloadType = payloadType;
        this.channelConstant = channelConstant;
        this.dtoPackage = dtoPackage;
        this.requiresModelImport = requiresModelImport;
    }

    @Override
    public TransportType getTransport() {
        return TransportType.KAFKA;
    }

    @Override
    public MessageHandlerDirection getDirection() {
        return MessageHandlerDirection.CONSUMER;
    }
}
