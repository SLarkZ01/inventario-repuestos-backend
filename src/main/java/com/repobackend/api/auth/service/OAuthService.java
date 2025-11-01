package com.repobackend.api.auth.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.io.IOException;

import com.repobackend.api.auth.exception.OAuthException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class OAuthService {
    private final ObjectMapper mapper = new ObjectMapper();
    private final HttpClient http;

    public OAuthService(HttpClient httpClient) {
        this.http = httpClient;
    }

    @Value("${app.oauth.google.client-id:}")
    private String googleClientId;

    // Verify Google ID token and check audience (aud) matches configured client id
    public Map<String, Object> verifyGoogleToken(String idToken) throws OAuthException {
        try {
            String url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + idToken;
            HttpRequest req = HttpRequest.newBuilder().uri(URI.create(url)).timeout(Duration.ofSeconds(3)).GET().build();
            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() != 200) throw new OAuthException("Google tokeninfo returned status=" + resp.statusCode(), resp.statusCode());
            Map<String, Object> data = mapper.readValue(resp.body(), new TypeReference<Map<String, Object>>() {});
            // verify audience
            Object aud = data.get("aud");
            if (googleClientId != null && !googleClientId.isBlank()) {
                if (!googleClientId.equals(String.valueOf(aud))) throw new OAuthException("Google token audience mismatch", 401);
            }
            // verify expiry
            Object expObj = data.get("exp");
            if (expObj != null) {
                try {
                    long exp = Long.parseLong(String.valueOf(expObj));
                    long now = System.currentTimeMillis() / 1000L;
                    if (exp < now) throw new OAuthException("Google token expired", 401);
                } catch (NumberFormatException nfe) {
                    throw new OAuthException("Invalid exp claim in Google token", 400);
                }
            }
            // verify issuer
            Object iss = data.get("iss");
            if (iss != null) {
                String issStr = String.valueOf(iss);
                if (!issStr.contains("accounts.google.com") && !issStr.contains("https://accounts.google.com")) throw new OAuthException("Invalid issuer in Google token", 401);
            }
            return data;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new OAuthException("Failed to verify Google token", ex, 502);
        }
    }
}
