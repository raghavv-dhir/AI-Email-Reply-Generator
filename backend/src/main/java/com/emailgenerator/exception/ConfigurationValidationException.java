package com.emailgenerator.exception;

/**
 * Thrown when required application configuration is missing or invalid.
 */
public class ConfigurationValidationException extends RuntimeException {

    public ConfigurationValidationException(String message) {
        super(message);
    }
}
