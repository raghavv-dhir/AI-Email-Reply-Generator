package com.emailgenerator.exception;

import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Instant;
import java.util.stream.Collectors;

/**
 * Global exception handler that returns safe, structured error responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                            HttpServletRequest request) {
        var validationErrors = ex.getBindingResult().getFieldErrors().stream()
                .collect(Collectors.toMap(FieldError::getField, FieldError::getDefaultMessage, (a, b) -> a));

        log.warn("Validation failed for request {}: {}", request.getRequestURI(), validationErrors);

        ErrorResponse body = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(HttpStatus.BAD_REQUEST.value())
                .error("Validation Failed")
                .message("Request validation failed")
                .path(request.getRequestURI())
                .validationErrors(validationErrors)
                .build();

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedJson(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        log.warn("Malformed JSON for request {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse body = buildError(HttpStatus.BAD_REQUEST, "Bad Request",
                "Malformed JSON request body", request.getRequestURI());

        return ResponseEntity.badRequest().body(body);
    }

    @ExceptionHandler(AiGenerationException.class)
    public ResponseEntity<ErrorResponse> handleAiGeneration(AiGenerationException ex, HttpServletRequest request) {
        log.error("AI generation error for request {}: {}", request.getRequestURI(), ex.getMessage());

        ErrorResponse body = buildError(HttpStatus.BAD_GATEWAY, "Bad Gateway",
                "Unable to generate email reply at this time", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(body);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException ex, HttpServletRequest request) {
        log.warn("API error for request {}: {}", request.getRequestURI(), ex.getMessage());

        HttpStatus status = HttpStatus.resolve(ex.getStatusCode());
        if (status == null) {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
        }

        ErrorResponse body = buildError(status, status.getReasonPhrase(), ex.getMessage(), request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    @ExceptionHandler(ConfigurationValidationException.class)
    public ResponseEntity<ErrorResponse> handleConfiguration(ConfigurationValidationException ex,
                                                             HttpServletRequest request) {
        log.error("Configuration error: {}", ex.getMessage());

        ErrorResponse body = buildError(HttpStatus.SERVICE_UNAVAILABLE, "Service Unavailable",
                "Service configuration is incomplete", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        log.error("Unexpected error for request {}", request.getRequestURI(), ex);

        ErrorResponse body = buildError(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error",
                "An unexpected error occurred", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(body);
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NoResourceFoundException ex, HttpServletRequest request) {
        ErrorResponse body = buildError(HttpStatus.NOT_FOUND, "Not Found",
                "Resource not found", request.getRequestURI());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }

    private ErrorResponse buildError(HttpStatus status, String error, String message, String path) {
        return ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(error)
                .message(message)
                .path(path)
                .build();
    }
}
