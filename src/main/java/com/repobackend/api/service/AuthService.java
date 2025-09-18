package com.repobackend.api.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.repobackend.api.dto.AuthRequests.LoginRequest;
import com.repobackend.api.dto.AuthRequests.RegisterRequest;
import com.repobackend.api.model.RefreshToken;
import com.repobackend.api.model.User;
import com.repobackend.api.repository.RefreshTokenRepository;
import com.repobackend.api.repository.UserRepository;
import com.repobackend.api.security.JwtUtil;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
    }

    public ResponseEntity<?> register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.email)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Email ya esta registrado");
        }
        if (userRepository.existsByUsername(req.username)) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Nombre de usuario ya esta en uso");
        }
        User u = new User();
        u.setUsername(req.username);
        u.setEmail(req.email);
        u.setPassword(passwordEncoder.encode(req.password));
        u.setNombre(req.nombre);
        u.setApellido(req.apellido);
        u.setRoles(Arrays.asList(req.rol != null ? req.rol : "VENDEDOR"));
        u.setActivo(true);
        u.setFechaCreacion(new Date());
        userRepository.save(u);
        return ResponseEntity.status(HttpStatus.CREATED).body(u);
    }

    public ResponseEntity<?> login(LoginRequest req) {
        Optional<User> maybe = userRepository.findByUsername(req.usernameOrEmail);
        if (maybe.isEmpty()) maybe = userRepository.findByEmail(req.usernameOrEmail);
        if (maybe.isEmpty()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        User u = maybe.get();
        if (!passwordEncoder.matches(req.password, u.getPassword())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        // Generate JWT access token and a random refresh token
        String accessToken = jwtUtil.generateToken(u.getId(), com.repobackend.api.config.SecurityConstants.JWT_EXPIRATION_MS);
        String rawRefresh = java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis();

        String refreshHash = sha256(rawRefresh);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(refreshHash);
        rt.setIssuedAt(new Date());
        rt.setExpiresAt(new Date(System.currentTimeMillis() + com.repobackend.api.config.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS));
        rt.setDeviceInfo(req.device);
        refreshTokenRepository.save(rt);

        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{
            put("accessToken", accessToken);
            put("refreshToken", rawRefresh);
            put("user", u);
        }});
    }

    private String sha256(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] h = md.digest(input.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(h);
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }

    public ResponseEntity<?> refresh(String refreshToken) {
        String h = sha256(refreshToken);
        Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(h);
        if (maybe.isEmpty()) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        RefreshToken rt = maybe.get();
        if (rt.isRevoked() || rt.getExpiresAt().before(new Date())) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Refresh token expired or revoked");
        }
        String newAccess = jwtUtil.generateToken(rt.getUserId(), com.repobackend.api.config.SecurityConstants.JWT_EXPIRATION_MS);
        return ResponseEntity.ok(new java.util.HashMap<String, Object>() {{ put("accessToken", newAccess); }});
    }

    public ResponseEntity<?> logout(String refreshToken) {
        String h = sha256(refreshToken);
        Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(h);
        if (maybe.isPresent()) {
            RefreshToken rt = maybe.get();
            rt.setRevoked(true);
            refreshTokenRepository.save(rt);
        }
        return ResponseEntity.ok().build();
    }
}
