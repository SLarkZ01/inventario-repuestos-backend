package com.repobackend.api.auth.config;

public class SecurityConstants {
    // 15 minutes
    public static final long JWT_EXPIRATION_MS = 15 * 60 * 1000;
    // 7 days
    public static final long REFRESH_TOKEN_EXPIRATION_MS = 7L * 24 * 60 * 60 * 1000;
}
