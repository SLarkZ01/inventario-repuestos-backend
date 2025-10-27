package com.repobackend.api.auth.exception;

public class OAuthException extends Exception {
    private final int statusCode;

    public OAuthException(String message, int statusCode) {
        super(message);
        this.statusCode = statusCode;
    }

    public OAuthException(String message, Throwable cause, int statusCode) {
        super(message, cause);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
