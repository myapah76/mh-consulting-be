package com.mhconsultingbe.shared.exception;

import com.mhconsultingbe.shared.response.ApiError;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.BindException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    ResponseEntity<ApiError> validation(Exception exception, HttpServletRequest request) {
        var errors = new LinkedHashMap<String, String>();
        var bindingResult = exception instanceof MethodArgumentNotValidException ex
                ? ex.getBindingResult() : ((BindException) exception).getBindingResult();
        bindingResult.getFieldErrors().forEach(error -> errors.putIfAbsent(error.getField(), error.getDefaultMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", errors, request);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> constraint(ConstraintViolationException exception, HttpServletRequest request) {
        var errors = new LinkedHashMap<String, String>();
        exception.getConstraintViolations().forEach(v -> errors.put(v.getPropertyPath().toString(), v.getMessage()));
        return error(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Request validation failed", errors, request);
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    ResponseEntity<ApiError> notFound(ResourceNotFoundException exception, HttpServletRequest request) {
        return error(HttpStatus.NOT_FOUND, "RESOURCE_NOT_FOUND", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(InvalidRequestException.class)
    ResponseEntity<ApiError> invalidRequest(InvalidRequestException exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> conflict(ConflictException exception, HttpServletRequest request) {
        return error(HttpStatus.CONFLICT, exception.getCode(), exception.getMessage(), Map.of(), request);
    }

    @ExceptionHandler(BadCredentialsException.class)
    ResponseEntity<ApiError> badCredentials(HttpServletRequest request) {
        return error(HttpStatus.UNAUTHORIZED, "AUTHENTICATION_FAILED", "Email or password is incorrect", Map.of(), request);
    }

    @ExceptionHandler(AccessDeniedException.class)
    ResponseEntity<ApiError> denied(HttpServletRequest request) {
        return error(HttpStatus.FORBIDDEN, "ACCESS_DENIED", "You do not have permission to perform this action", Map.of(), request);
    }

    @ExceptionHandler({MethodArgumentTypeMismatchException.class, MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class, IllegalArgumentException.class})
    ResponseEntity<ApiError> invalidParameter(Exception exception, HttpServletRequest request) {
        return error(HttpStatus.BAD_REQUEST, "INVALID_PARAMETER", "A query or path parameter is invalid", Map.of(), request);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> dataConflict(DataIntegrityViolationException exception, HttpServletRequest request) {
        log.warn("Database constraint rejected request at {}", request.getRequestURI());
        return error(HttpStatus.CONFLICT, "DATA_CONFLICT", "The requested value conflicts with existing data", Map.of(), request);
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> unexpected(Exception exception, HttpServletRequest request) {
        log.error("Unexpected error processing {}", request.getRequestURI(), exception);
        return error(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "An unexpected error occurred", Map.of(), request);
    }

    private ResponseEntity<ApiError> error(HttpStatus status, String code, String message,
                                           Map<String, String> fields, HttpServletRequest request) {
        return ResponseEntity.status(status).body(new ApiError(
                Instant.now(), status.value(), code, message, fields, request.getRequestURI()));
    }
}
