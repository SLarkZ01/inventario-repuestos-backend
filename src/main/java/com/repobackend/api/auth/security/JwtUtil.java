package com.repobackend.api.auth.security;

import java.security.Key;
import java.util.Date;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

@Component
public class JwtUtil {
    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);
    private final Key key;

    public JwtUtil(@Value("${app.jwt.secret:replace_this_secret_change_in_prod}") String secret) {
        if (secret == null || secret.isBlank() || secret.equals("replace_this_secret_change_in_prod")) {
            logger.warn("JWT secret is using default placeholder — change APP_JWT_SECRET in production");
        }
        // Ensure secret has sufficient length for HS256 (at least 32 bytes)
        byte[] secretBytes = secret == null ? new byte[0] : secret.getBytes();
        if (secretBytes.length < 32) {
            throw new IllegalArgumentException("JWT secret is too short; provide at least 32 bytes of entropy");
        }
        this.key = Keys.hmacShaKeyFor(secretBytes);
    }

    public String generateToken(String subject, long expirationMs) {
        Date now = new Date();
        Date exp = new Date(now.getTime() + expirationMs);
        return Jwts.builder()
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(exp)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
    }
}
