package com.repobackend.api.service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.io.IOException;

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

    @Value("${app.oauth.facebook.app-id:}")
    private String facebookAppId;

    @Value("${app.oauth.facebook.app-secret:}")
    private String facebookAppSecret;

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

    // Verify Facebook access token: first call debug_token to verify app_id, then fetch user info
    public Map<String, Object> verifyFacebookToken(String accessToken) throws OAuthException {
        try {
            // debug_token endpoint requires app access token: {app-id}|{app-secret}
            String appAccess = facebookAppId + "|" + facebookAppSecret;
            String dbgUrl = "https://graph.facebook.com/debug_token?input_token=" + accessToken + "&access_token=" + appAccess;
            HttpRequest dbgReq = HttpRequest.newBuilder().uri(URI.create(dbgUrl)).timeout(Duration.ofSeconds(3)).GET().build();
            HttpResponse<String> dbgResp = http.send(dbgReq, HttpResponse.BodyHandlers.ofString());
            if (dbgResp.statusCode() != 200) throw new OAuthException("Facebook debug_token status=" + dbgResp.statusCode(), dbgResp.statusCode());
            Map<String, Object> debugObj = mapper.readValue(dbgResp.body(), new TypeReference<Map<String, Object>>() {});
            Map<String, Object> data = mapper.convertValue(debugObj.get("data"), new TypeReference<Map<String, Object>>() {});
            if (data == null) throw new OAuthException("Facebook debug_token returned no data", 400);
            // check app_id matches and token is valid
            Object appId = data.get("app_id");
            if (facebookAppId != null && !facebookAppId.isBlank()) {
                if (!facebookAppId.equals(String.valueOf(appId))) return null;
            }
            Object isValidObj = data.get("is_valid");
            boolean isValid = false;
            if (isValidObj instanceof Boolean b) isValid = b; else if (isValidObj != null) isValid = Boolean.parseBoolean(String.valueOf(isValidObj));
            if (!isValid) throw new OAuthException("Facebook token not valid", 401);

            // now call me endpoint to get id,name,email
            String meUrl = "https://graph.facebook.com/me?fields=id,name,email&access_token=" + accessToken;
            HttpRequest meReq = HttpRequest.newBuilder().uri(URI.create(meUrl)).timeout(Duration.ofSeconds(3)).GET().build();
            HttpResponse<String> meResp = http.send(meReq, HttpResponse.BodyHandlers.ofString());
            if (meResp.statusCode() != 200) throw new OAuthException("Facebook /me returned status=" + meResp.statusCode(), meResp.statusCode());
            Map<String, Object> userInfo = mapper.readValue(meResp.body(), new TypeReference<Map<String, Object>>() {});
            return userInfo;
        } catch (IOException | InterruptedException ex) {
            if (ex instanceof InterruptedException) Thread.currentThread().interrupt();
            throw new OAuthException("Failed to verify Facebook token", ex, 502);
        }
    }
}
