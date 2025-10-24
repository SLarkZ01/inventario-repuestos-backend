package com.repobackend.api.auth.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.auth.dto.AuthRequests.LoginRequest;
import com.repobackend.api.auth.dto.AuthRequests.RegisterRequest;
import com.repobackend.api.auth.service.AuthService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        return authService.login(req, request);
    }

    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> body, HttpServletRequest request) {
        String refresh = body.get("refreshToken");
        return authService.refresh(refresh, request);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody java.util.Map<String, String> body) {
        String refresh = body.get("refreshToken");
        return authService.logout(refresh);
    }

    @PostMapping("/oauth/google")
    public ResponseEntity<?> oauthGoogle(@RequestBody java.util.Map<String, String> body) {
        String idToken = body.get("idToken");
        String inviteCode = body.get("inviteCode");
        String device = body.get("device");
        return authService.oauthLoginGoogle(idToken, inviteCode, device);
    }

    @PostMapping("/oauth/facebook")
    public ResponseEntity<?> oauthFacebook(@RequestBody java.util.Map<String, String> body) {
        String accessToken = body.get("accessToken");
        String inviteCode = body.get("inviteCode");
        String device = body.get("device");
        return authService.oauthLoginFacebook(accessToken, inviteCode, device);
    }

    @PostMapping("/revoke-all")
    public ResponseEntity<?> revokeAll(org.springframework.security.core.Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        return authService.revokeAllRefreshTokensForUser(userId);
    }

    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        return authService.getCurrentUserById(userId);
    }
}
