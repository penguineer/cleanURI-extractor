package com.penguineering.cleanuri.extractor.tasks;


import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Introspected;

import java.net.URI;
import java.util.Set;

@Introspected
public class ExtractionTask {
    public enum Meta {
        TITLE,
        PRICE
    };

    private final URI reduced_uri;
    private final URI uri;
    private final Set<Meta> meta;

    @Internal
    ExtractionTask(URI uri, URI reduced_uri, Set<Meta> meta) {
        this.uri = uri;
        this.reduced_uri = reduced_uri;
        this.meta = meta;
    }

    public URI getUri() {
        return uri;
    }

    public URI getReduced_uri() {
        return reduced_uri;
    }

    public Set<Meta> getMeta() {
        return meta;
    }
}
