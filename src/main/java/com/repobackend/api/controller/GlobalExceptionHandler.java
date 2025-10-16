package com.repobackend.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindException;
import jakarta.validation.ConstraintViolationException;
// imports consolidated above

import com.repobackend.api.service.OAuthException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(OAuthException.class)
    public ResponseEntity<?> handleOAuthException(OAuthException ex) {
        int status = ex.getStatusCode() == 0 ? 502 : ex.getStatusCode();
        return ResponseEntity.status(status).body(Map.of("error", ex.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<?> handleGeneric(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "Internal server error"));
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
}
