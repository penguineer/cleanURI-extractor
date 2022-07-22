package com.penguineering.cleanuri.extractor.tasks;

import io.micronaut.core.annotation.Introspected;

@Introspected
public class ErrorResult {
    private final int code;
    private final String error;

    public ErrorResult(int code, String error) {
        this.code = code;
        this.error = error;
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }
}
