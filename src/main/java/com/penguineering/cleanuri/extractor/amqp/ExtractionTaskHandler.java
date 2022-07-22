package com.penguineering.cleanuri.extractor.amqp;

import com.penguineering.cleanuri.extractor.processors.Extractor;
import com.penguineering.cleanuri.extractor.processors.ExtractorException;
import com.penguineering.cleanuri.extractor.processors.Metakey;
import com.penguineering.cleanuri.extractor.tasks.ErrorResult;
import com.penguineering.cleanuri.extractor.tasks.ExtractionResult;
import com.penguineering.cleanuri.extractor.tasks.ExtractionTask;
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
    ErrorEmitter errorEmitter;

    @Inject
    ExtractionResultEmitter extractionResultEmitter;

    @Inject
    List<Extractor> extractors;

    @Queue("${extractor.extraction-task-queue}")
    public void receive(@RabbitProperty("correlationId") String correlationId,
                        @RabbitProperty("replyTo") String replyTo,
                        final ExtractionTask task,
                        RabbitAcknowledgement acknowledgement) {
        final URI uri = task.getReduced_uri();
        final ExtractionResult.Builder extractionResultBuilder = ExtractionResult.Builder.fromExtractionTask(task);

        if (task.getMeta() != null && !task.getMeta().isEmpty()) {
            Optional<Extractor> extractor = extractors.stream()
                    .filter(e -> e.isSuitable(uri))
                    .findFirst();

            if (extractor.isPresent()) {
                try {
                    final Map<Metakey, String> meta = extractor.get().extractMetadata(uri);
                    extractionResultBuilder.putAllMeta(meta);
                } catch (ExtractorException e) {
                    errorEmitter.send(correlationId, replyTo, new ErrorResult(
                            400, e.getMessage()
                    ));
                }
            } else {
                // This should not happen!
                errorEmitter.send(correlationId, replyTo, new ErrorResult(
                        404, "Could not find a matching extractor!"
                ));
            }
        }

        // send the result even if the extraction failed
        // we still have the reduction and the HTTP endpoint will already have failed and ignores this
        extractionResultEmitter.send(correlationId, replyTo, extractionResultBuilder.instance());

        acknowledgement.ack();
    }
}
