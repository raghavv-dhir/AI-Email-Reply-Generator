package com.emailgenerator.exception;

import lombok.Getter;

/**
 * Generic API exception with a safe client-facing message.
 */
@Getter
public class ApiException extends RuntimeException {

    private final int statusCode;

    public ApiException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }
}
