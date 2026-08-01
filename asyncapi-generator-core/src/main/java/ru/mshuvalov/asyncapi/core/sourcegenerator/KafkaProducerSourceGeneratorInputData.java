package ru.mshuvalov.asyncapi.core.sourcegenerator;

public final class KafkaProducerSourceGeneratorInputData implements SourceGeneratorInputData {
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

}
