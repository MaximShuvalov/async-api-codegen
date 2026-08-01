package ru.mshuvalov.asyncapi.core.sourcegenerator;

public final class KafkaConsumerSourceGeneratorInputData implements SourceGeneratorInputData {
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

}
