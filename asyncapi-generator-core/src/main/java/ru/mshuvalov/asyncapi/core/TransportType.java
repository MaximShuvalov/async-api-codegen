package ru.mshuvalov.asyncapi.core;

import java.util.Arrays;
import java.util.Optional;

/** Standard AsyncAPI protocol-binding identifiers supported by the generator architecture. */
public enum TransportType {
    KAFKA("kafka"),
    AMQP("amqp"),
    MQTT("mqtt"),
    WEBSOCKET("ws"),
    HTTP("http"),
    NATS("nats"),
    SQS("sqs"),
    REDIS("redis");

    private final String bindingName;
    TransportType(String bindingName) { this.bindingName = bindingName; }
    public String bindingName() { return bindingName; }
    public static Optional<TransportType> fromBinding(String bindingName) {
        return Arrays.stream(values()).filter(type -> type.bindingName.equals(bindingName)).findFirst();
    }
}
