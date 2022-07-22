package com.penguineering.cleanuri.extractor.amqp;

import com.penguineering.cleanuri.extractor.tasks.ExtractionResult;
import io.micronaut.rabbitmq.annotation.Binding;
import io.micronaut.rabbitmq.annotation.RabbitClient;
import io.micronaut.rabbitmq.annotation.RabbitProperty;

@RabbitClient
public interface ExtractionResultEmitter {
    @RabbitProperty(name = "contentType", value = "application/json")
    void send(@RabbitProperty("correlationId") String correlationId,
              @Binding String replyTo,
              ExtractionResult result);
}
