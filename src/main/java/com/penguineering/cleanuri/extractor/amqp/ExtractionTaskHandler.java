package com.penguineering.cleanuri.extractor.amqp;

import com.penguineering.cleanuri.common.amqp.ExtractionTaskEmitter;
import com.penguineering.cleanuri.common.message.ExtractionTask;
import com.penguineering.cleanuri.common.message.MetaData;
import com.penguineering.cleanuri.extractor.processors.Extractor;
import com.penguineering.cleanuri.extractor.processors.ExtractorException;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RabbitListener
public class ExtractionTaskHandler {
    @Inject
    ExtractionTaskEmitter emitter;

    @Inject
    List<Extractor> extractors;

    @Queue("${extractor.extraction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ExtractionTask task,
                        RabbitAcknowledgement acknowledgement) {
        final ExtractionTask.Builder taskBuilder = ExtractionTask.Builder.copy(task);


        final URI uri = task.getCanonizedURI();

        if (!task.getRequest().getFields().isEmpty()) {
            Optional<Extractor> extractor = extractors.stream()
                    .filter(e -> e.isSuitable(uri))
                    .findFirst();

            if (extractor.isPresent()) {
                try {
                    final Map<MetaData.Fields, String> meta = extractor.get().extractMetadata(uri);
                    meta.forEach((key, value) -> taskBuilder.putMeta(
                            key,
                            MetaData.Builder.withValue(value).instance()
                    ));
                } catch (ExtractorException e) {
                    taskBuilder.addError(e.getMessage());
                }
            } else {
                taskBuilder.addError("Could not find a matching extractor!");
            }
        }

        emitter.send(replyTo, correlationId, null, taskBuilder.instance());

        acknowledgement.ack();
    }
}
