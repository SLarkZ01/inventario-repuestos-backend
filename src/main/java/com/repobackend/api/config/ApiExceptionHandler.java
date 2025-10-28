package com.repobackend.api.config;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;

import jakarta.validation.ConstraintViolationException;

import com.repobackend.api.auth.exception.OAuthException;
import org.springframework.web.server.ResponseStatusException;

@RestControllerAdvice
public class ApiExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(ApiExceptionHandler.class);

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<?> handleResponseStatusException(ResponseStatusException ex) {
        int code = ex.getStatusCode() == null ? HttpStatus.INTERNAL_SERVER_ERROR.value() : ex.getStatusCode().value();
        String msg = ex.getReason() == null ? ex.getMessage() : ex.getReason();
        return ResponseEntity.status(code).body(Map.of("error", msg));
    }

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<?> handleOAuthException(OAuthException ex) {
        int status = ex.getStatusCode() == 0 ? 502 : ex.getStatusCode();
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException ex) {
        var errors = new java.util.HashMap<String, String>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("errors", errors));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<?> handleBind(BindException ex) {
        var errors = new java.util.HashMap<String, String>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            errors.put(fe.getField(), fe.getDefaultMessage());
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("errors", errors));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<?> handleConstraintViolation(ConstraintViolationException ex) {
        var errors = new java.util.HashMap<String, String>();
        ex.getConstraintViolations().forEach(cv -> {
            String path = cv.getPropertyPath() == null ? "" : cv.getPropertyPath().toString();
            errors.put(path, cv.getMessage());
        });
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(java.util.Map.of("errors", errors));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleAll(Exception ex) {
        logger.error("Unhandled exception:", ex);
        String msg = ex.getMessage() == null ? "Internal server error" : ex.getMessage();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", msg));
    }
}
