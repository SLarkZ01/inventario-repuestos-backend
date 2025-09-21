package com.repobackend.api.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.repobackend.api.dto.AuthRequests.LoginRequest;
import com.repobackend.api.dto.AuthRequests.RegisterRequest;
import com.repobackend.api.service.TallerService;
import com.repobackend.api.service.OAuthService;
import java.util.Map;
import com.repobackend.api.model.RefreshToken;
import com.repobackend.api.model.User;
import com.repobackend.api.repository.RefreshTokenRepository;
import com.repobackend.api.repository.UserRepository;
import com.repobackend.api.security.JwtUtil;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
    private final JwtUtil jwtUtil;
    private final TallerService tallerService;
    private final OAuthService oauthService;

    public AuthService(UserRepository userRepository, RefreshTokenRepository refreshTokenRepository, JwtUtil jwtUtil,
            TallerService tallerService, OAuthService oauthService) {
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
        this.jwtUtil = jwtUtil;
        this.tallerService = tallerService;
        this.oauthService = oauthService;
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
        // If the user registers with an inviteCode, do NOT give global ADMIN role by default.
        // If there's no inviteCode, default to ADMIN (user creates talleres).
        if (req.inviteCode != null && !req.inviteCode.isBlank()) {
            u.setRoles(Arrays.asList(req.rol != null ? req.rol : "USER"));
        } else {
            u.setRoles(Arrays.asList(req.rol != null ? req.rol : "ADMIN"));
        }
        u.setActivo(true);
        u.setFechaCreacion(new Date());
        userRepository.save(u);
        Map<String, Object> joined = null;
        if (req.inviteCode != null && !req.inviteCode.isBlank()) {
            joined = tallerService.acceptInvitationByCode(u.getId(), req.inviteCode);
        }
        // generate tokens
        String accessToken = jwtUtil.generateToken(u.getId(), com.repobackend.api.config.SecurityConstants.JWT_EXPIRATION_MS);
        String rawRefresh = java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        String refreshHash = sha256(rawRefresh);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(refreshHash);
        rt.setIssuedAt(new Date());
        rt.setExpiresAt(new Date(System.currentTimeMillis() + com.repobackend.api.config.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS));
        refreshTokenRepository.save(rt);
        var resp = new java.util.HashMap<String, Object>();
        resp.put("accessToken", accessToken);
        resp.put("refreshToken", rawRefresh);
        resp.put("user", u);
        if (joined != null) resp.put("joined", joined);
        return ResponseEntity.status(HttpStatus.CREATED).body(resp);
    }


    public ResponseEntity<?> login(LoginRequest req, HttpServletRequest request) {
        Optional<User> maybe = userRepository.findByUsername(req.usernameOrEmail);
        if (maybe.isEmpty()) maybe = userRepository.findByEmail(req.usernameOrEmail);
        if (maybe.isEmpty()) {
            String ip = request == null ? "-" : request.getRemoteAddr();
            String ua = request == null ? "-" : request.getHeader("User-Agent");
            logger.warn("Login failed for user/email: {} ip={} ua={} - not found", req.usernameOrEmail, ip, ua);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciales inválidas");
        }
        User u = maybe.get();
        if (!passwordEncoder.matches(req.password, u.getPassword())) {
            String ip = request == null ? "-" : request.getRemoteAddr();
            String ua = request == null ? "-" : request.getHeader("User-Agent");
            logger.warn("Login failed for user/email: {} ip={} ua={} - invalid password", req.usernameOrEmail, ip, ua);
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

    public ResponseEntity<?> refresh(String refreshToken, HttpServletRequest request) {
        String h = sha256(refreshToken);
        Optional<RefreshToken> maybe = refreshTokenRepository.findByTokenHash(h);
        if (maybe.isEmpty()) {
            String ip = request == null ? "-" : request.getRemoteAddr();
            String ua = request == null ? "-" : request.getHeader("User-Agent");
            logger.warn("Refresh attempt with invalid token hash ip={} ua={}", ip, ua);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid refresh token");
        }
        RefreshToken rt = maybe.get();
        if (rt.isRevoked() || rt.getExpiresAt().before(new Date())) {
            logger.warn("Refresh token expired or revoked for userId={}", rt.getUserId());
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

    public ResponseEntity<?> oauthLoginGoogle(String idToken, String inviteCode, String device) {
        Map<String, Object> info;
        try {
            info = oauthService.verifyGoogleToken(idToken);
        } catch (com.repobackend.api.service.OAuthException oex) {
            return ResponseEntity.status(oex.getStatusCode() == 0 ? HttpStatus.BAD_GATEWAY : HttpStatus.valueOf(oex.getStatusCode())).body(oex.getMessage());
        }
        if (info == null || info.get("email") == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Google inválido");
        String email = (String) info.get("email");
        String name = (String) info.getOrDefault("name", "");
        // Prefer to find by providerId if present
        Object sub = info.get("sub");
        Optional<User> maybe = Optional.empty();
        if (sub != null) {
            maybe = userRepository.findByProviderAndProviderId("google", String.valueOf(sub));
        }
        if (maybe.isEmpty()) maybe = userRepository.findByEmail(email);
        User u;
        if (maybe.isPresent()) {
            u = maybe.get();
            // ensure provider info is set when missing
            if (sub != null && (u.getProvider() == null || u.getProviderId() == null)) {
                u.setProvider("google");
                u.setProviderId(String.valueOf(sub));
                userRepository.save(u);
            }
        } else {
            u = new User();
            String generatedUsername = email.split("@")[0] + java.util.UUID.randomUUID().toString().substring(0,4);
            u.setUsername(generatedUsername);
            u.setEmail(email);
            u.setNombre(name);
            u.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            if (inviteCode != null && !inviteCode.isBlank()) u.setRoles(Arrays.asList("USER")); else u.setRoles(Arrays.asList("ADMIN"));
            // set provider info from Google token
            if (sub != null) {
                u.setProvider("google");
                u.setProviderId(String.valueOf(sub));
            }
            u.setActivo(true);
            u.setFechaCreacion(new Date());
            userRepository.save(u);
        }
        Map<String, Object> joined = null;
        if (inviteCode != null && !inviteCode.isBlank()) {
            joined = tallerService.acceptInvitationByCode(u.getId(), inviteCode);
        }
        String accessToken = jwtUtil.generateToken(u.getId(), com.repobackend.api.config.SecurityConstants.JWT_EXPIRATION_MS);
        String rawRefresh = java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        String refreshHash = sha256(rawRefresh);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(refreshHash);
        rt.setIssuedAt(new Date());
        rt.setExpiresAt(new Date(System.currentTimeMillis() + com.repobackend.api.config.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS));
        rt.setDeviceInfo(device);
        refreshTokenRepository.save(rt);
        var resp = new java.util.HashMap<String, Object>();
        resp.put("accessToken", accessToken);
        resp.put("refreshToken", rawRefresh);
        resp.put("user", u);
        if (joined != null) resp.put("joined", joined);
        return ResponseEntity.ok(resp);
    }

    public ResponseEntity<?> oauthLoginFacebook(String accessTokenFb, String inviteCode, String device) {
        Map<String, Object> info;
        try {
            info = oauthService.verifyFacebookToken(accessTokenFb);
        } catch (com.repobackend.api.service.OAuthException oex) {
            return ResponseEntity.status(oex.getStatusCode() == 0 ? HttpStatus.BAD_GATEWAY : HttpStatus.valueOf(oex.getStatusCode())).body(oex.getMessage());
        }
        if (info == null || info.get("email") == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Token Facebook inválido");
        String email = (String) info.get("email");
        String name = (String) info.getOrDefault("name", "");
        // Prefer to find by providerId if present
        Object fbId = info.get("id");
        Optional<User> maybe = Optional.empty();
        if (fbId != null) {
            maybe = userRepository.findByProviderAndProviderId("facebook", String.valueOf(fbId));
        }
        if (maybe.isEmpty()) maybe = userRepository.findByEmail(email);
        User u;
        if (maybe.isPresent()) {
            u = maybe.get();
            // ensure provider info is set when missing
            if (fbId != null && (u.getProvider() == null || u.getProviderId() == null)) {
                u.setProvider("facebook");
                u.setProviderId(String.valueOf(fbId));
                userRepository.save(u);
            }
        } else {
            u = new User();
            String generatedUsername = email.split("@")[0] + java.util.UUID.randomUUID().toString().substring(0,4);
            u.setUsername(generatedUsername);
            u.setEmail(email);
            u.setNombre(name);
            u.setPassword(passwordEncoder.encode(java.util.UUID.randomUUID().toString()));
            if (inviteCode != null && !inviteCode.isBlank()) u.setRoles(Arrays.asList("USER")); else u.setRoles(Arrays.asList("ADMIN"));
            // set provider info from Facebook response
            if (fbId != null) {
                u.setProvider("facebook");
                u.setProviderId(String.valueOf(fbId));
            }
            u.setActivo(true);
            u.setFechaCreacion(new Date());
            userRepository.save(u);
        }
        Map<String, Object> joined = null;
        if (inviteCode != null && !inviteCode.isBlank()) {
            joined = tallerService.acceptInvitationByCode(u.getId(), inviteCode);
        }
        String accessToken = jwtUtil.generateToken(u.getId(), com.repobackend.api.config.SecurityConstants.JWT_EXPIRATION_MS);
        String rawRefresh = java.util.UUID.randomUUID().toString() + "-" + System.currentTimeMillis();
        String refreshHash = sha256(rawRefresh);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(u.getId());
        rt.setTokenHash(refreshHash);
        rt.setIssuedAt(new Date());
        rt.setExpiresAt(new Date(System.currentTimeMillis() + com.repobackend.api.config.SecurityConstants.REFRESH_TOKEN_EXPIRATION_MS));
        rt.setDeviceInfo(device);
        refreshTokenRepository.save(rt);
        var resp = new java.util.HashMap<String, Object>();
        resp.put("accessToken", accessToken);
        resp.put("refreshToken", rawRefresh);
        resp.put("user", u);
        if (joined != null) resp.put("joined", joined);
        return ResponseEntity.ok(resp);
    }
}
