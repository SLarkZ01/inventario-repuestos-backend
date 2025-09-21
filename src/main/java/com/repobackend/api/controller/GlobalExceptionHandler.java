package com.repobackend.api.controller;

import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

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
}
