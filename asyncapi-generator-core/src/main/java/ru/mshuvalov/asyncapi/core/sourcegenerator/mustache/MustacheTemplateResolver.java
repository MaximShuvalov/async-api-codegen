package ru.mshuvalov.asyncapi.core.sourcegenerator.mustache;

import ru.mshuvalov.asyncapi.core.sourcegenerator.KafkaConsumerSourceGeneratorInputData;
import ru.mshuvalov.asyncapi.core.sourcegenerator.KafkaProducerSourceGeneratorInputData;
import ru.mshuvalov.asyncapi.core.sourcegenerator.SourceGeneratorInputData;

public class MustacheTemplateResolver {
    public static String resolve(SourceGeneratorInputData inputData){
        switch (inputData){
            case KafkaProducerSourceGeneratorInputData ignored -> {
                return "templates/kafka/kafka-producer.java.mustache";
            }
            case KafkaConsumerSourceGeneratorInputData ignored ->{
                return "templates/kafka/kafka-listener-adapter.java.mustache";
            }
        }
    }
}
