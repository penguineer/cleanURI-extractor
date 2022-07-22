package com.penguineering.cleanuri.extractor.tasks;

import com.penguineering.cleanuri.extractor.processors.Metakey;
import io.micronaut.context.annotation.Bean;
import io.micronaut.core.annotation.Internal;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@Bean
public class ExtractionResult {
    public static class Builder {
        public static Builder fromExtractionTask(ExtractionTask task) {
            return new Builder(task.getUri(), task.getReduced_uri());
        }

        private final URI uri;
        private final URI reduced;
        private Map<Metakey, String> meta;

        public Builder(URI uri, URI reduced) {
            this.uri = uri;
            this.reduced = reduced;
            this.meta = null;
        }

        public Builder putMeta(Metakey key, String value) {
            if (this.meta == null)
                this.meta = new HashMap<>();
            this.meta.put(key, value);
            return this;
        }

        public Builder putAllMeta(Map<Metakey, String> meta) {
            if (this.meta == null)
                this.meta = new HashMap<>();
            this.meta.putAll(meta);
            return this;
        }

        public ExtractionResult instance() {
            final ExtractionResult instance = new ExtractionResult(uri, reduced, meta);
            this.meta = null;
            return instance;
        }
    }

    private final URI uri;
    private final URI reduced;
    private final Map<Metakey, String> meta;

    @Internal
    ExtractionResult(URI uri, URI reduced, Map<Metakey, String> meta) {
        this.uri = uri;
        this.reduced = reduced;
        this.meta = meta;
    }

    public URI getUri() {
        return uri;
    }

    public URI getReduced() {
        return reduced;
    }

    public Map<Metakey, String> getMeta() {
        return meta;
    }
}
