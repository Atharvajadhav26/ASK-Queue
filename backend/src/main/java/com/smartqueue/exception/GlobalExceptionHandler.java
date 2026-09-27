package com.smartqueue.exception;

import com.smartqueue.dto.response.ApiError;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(QueueNotFoundException.class)
    public ResponseEntity<ApiError> handleQueueNotFoundException(QueueNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Queue Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(TokenNotFoundException.class)
    public ResponseEntity<ApiError> handleTokenNotFoundException(TokenNotFoundException ex) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Token Not Found", ex.getMessage(), null);
    }

    @ExceptionHandler(QueueClosedException.class)
    public ResponseEntity<ApiError> handleQueueClosedException(QueueClosedException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Queue Closed", ex.getMessage(), null);
    }

    @ExceptionHandler(QueueFullException.class)
    public ResponseEntity<ApiError> handleQueueFullException(QueueFullException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Queue Full", ex.getMessage(), null);
    }

    @ExceptionHandler(InvalidTokenStateException.class)
    public ResponseEntity<ApiError> handleInvalidTokenStateException(InvalidTokenStateException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Invalid Token State", ex.getMessage(), null);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ApiError> handleDuplicateResourceException(DuplicateResourceException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Duplicate Resource", ex.getMessage(), null);
    }

    @ExceptionHandler(ConcurrentQueueOperationException.class)
    public ResponseEntity<ApiError> handleConcurrentQueueOperationException(ConcurrentQueueOperationException ex) {
        return buildErrorResponse(HttpStatus.CONFLICT, "Concurrent Operation", ex.getMessage(), null);
    }

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiError> handleUnauthorizedException(UnauthorizedException ex) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, "Unauthorized", ex.getMessage(), null);
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiError> handleBadRequestException(BadRequestException ex) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Bad Request", ex.getMessage(), null);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Validation Error", "Invalid request body", errors);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> handleGenericException(Exception ex) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal Server Error", "An unexpected error occurred", null);
    }

    private ResponseEntity<ApiError> buildErrorResponse(HttpStatus status, String error, String message, Map<String, String> details) {
        ApiError apiError = new ApiError(status.value(), error, message, LocalDateTime.now(), details);
        return new ResponseEntity<>(apiError, status);
    }
}
