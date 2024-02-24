package com.penguineering.cleanuri.extractor.amqp;

import com.penguineering.cleanuri.common.amqp.ExtractionTaskEmitter;
import com.penguineering.cleanuri.common.message.ExtractionTask;
import com.penguineering.cleanuri.common.message.MetaData;
import com.penguineering.cleanuri.site.Extractor;
import com.penguineering.cleanuri.site.Site;
import com.penguineering.cleanuri.site.data.ProductDescription;
import io.micronaut.rabbitmq.annotation.Queue;
import io.micronaut.rabbitmq.annotation.RabbitListener;
import io.micronaut.rabbitmq.annotation.RabbitProperty;
import io.micronaut.rabbitmq.bind.RabbitAcknowledgement;
import jakarta.inject.Inject;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

@RabbitListener
public class ExtractionTaskHandler {
    @Inject
    ExtractionTaskEmitter emitter;

    @Inject
    List<Site> sites;

    @Queue("${extractor.extraction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ExtractionTask task,
                        RabbitAcknowledgement acknowledgement) {
        final ExtractionTask.Builder taskBuilder = ExtractionTask.Builder.copy(task);


        final URI uri = task.getCanonizedURI();

        try {
            if (!task.getRequest().getFields().isEmpty()) {
                Extractor extractor = sites.stream()
                        .filter(s -> s.canProcessURI(uri))
                        .findAny()
                        .flatMap(s -> s.newExtractor(uri))
                        .map(c -> (Extractor) c.withExceptionHandler((l, e) -> {
                            // add error if level is warning or higher
                            if (l.intValue() <= Level.WARNING.intValue())
                                taskBuilder.addError(e.getMessage());
                        }))
                        .orElseThrow(() -> new IllegalArgumentException("Could not find a matching extractor!"));

                ProductDescription productDescription = extractor.extractProductDescription()
                        .orElseThrow(() -> new IllegalArgumentException("Could not extract product description!"));

                var metaData = Map.of(
                        MetaData.Fields.ID, productDescription.getId(),
                        MetaData.Fields.TITLE, productDescription.getName()
                );
                metaData.entrySet().stream()
                        .filter(e -> e.getValue().isPresent())
                        .filter(e -> task.getRequest().getFields().contains(e.getKey()))
                        .forEach(e -> taskBuilder.putMeta(e.getKey(), MetaData.Builder.withValue(e.getValue().get()).instance()));
            }
        } catch (IllegalArgumentException e) {
            taskBuilder.addError(e.getMessage());
        }

        emitter.send(replyTo, correlationId, null, taskBuilder.instance());

        acknowledgement.ack();
    }
}
