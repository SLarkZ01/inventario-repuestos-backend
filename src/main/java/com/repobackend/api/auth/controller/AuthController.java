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

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "Autenticación y gestión de tokens")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(
        summary = "Registro de usuario",
        description = "Registra un usuario nuevo en el sistema. Opcionalmente puede incluir un código de invitación para unirse a un taller.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del nuevo usuario",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de registro",
                    value = "{\"username\":\"jperez\",\"email\":\"jperez@example.com\",\"password\":\"securePass123\",\"nombre\":\"Juan\",\"apellido\":\"Pérez\",\"inviteCode\":\"ABC123\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Usuario creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"message\":\"Usuario registrado exitosamente\",\"userId\":\"507f1f77bcf86cd799439011\"}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe", content = @Content)
        }
    )
    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        return authService.register(req);
    }

    @Operation(
        summary = "Login",
        description = "Inicia sesión con username/email y contraseña. Devuelve tokens JWT de acceso y refresh.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Credenciales de acceso",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de login",
                    value = "{\"usernameOrEmail\":\"jperez\",\"password\":\"securePass123\",\"device\":\"web\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Login exitoso",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        name = "Respuesta exitosa",
                        value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"refreshToken\":\"r1234567890abcdef\",\"user\":{\"id\":\"507f1f77bcf86cd799439011\",\"username\":\"jperez\",\"email\":\"jperez@example.com\"}}"
                    )
                )
            ),
            @ApiResponse(responseCode = "401", description = "Credenciales inválidas", content = @Content)
        }
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req, HttpServletRequest request) {
        return authService.login(req, request);
    }

    @Operation(
        summary = "Refresh token",
        description = "Renueva el token de acceso usando un refresh token válido",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"refreshToken\":\"r1234567890abcdef\"}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Tokens renovados",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"refreshToken\":\"r9876543210fedcba\"}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Refresh token inválido o expirado", content = @Content)
        }
    )
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(@RequestBody java.util.Map<String, String> body, HttpServletRequest request) {
        String refresh = body.get("refreshToken");
        return authService.refresh(refresh, request);
    }

    @Operation(summary = "Logout", description = "Revoca el refresh token dado",
        responses = {@ApiResponse(responseCode = "200", description = "Logout realizado", content = @Content)})
    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody java.util.Map<String, String> body) {
        String refresh = body.get("refreshToken");
        return authService.logout(refresh);
    }

    @Operation(
        summary = "OAuth Google",
        description = "Autenticación/registro usando Google OAuth. Requiere el idToken obtenido del flujo OAuth de Google.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Token de Google y datos opcionales",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Login con Google",
                    value = "{\"idToken\":\"eyJhbGciOiJSUzI1NiIsImtpZCI6IjAx...\",\"inviteCode\":\"TALLER-ABC123\",\"device\":\"android\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "OAuth exitoso",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"accessToken\":\"eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...\",\"refreshToken\":\"r1234567890\",\"user\":{\"id\":\"507f1f77bcf86cd799439011\",\"email\":\"user@gmail.com\"}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Token de Google inválido", content = @Content)
        }
    )
    @PostMapping("/oauth/google")
    public ResponseEntity<?> oauthGoogle(@RequestBody java.util.Map<String, String> body) {
        String idToken = body.get("idToken");
        String inviteCode = body.get("inviteCode");
        String device = body.get("device");
        return authService.oauthLoginGoogle(idToken, inviteCode, device);
    }

    @Operation(summary = "Revocar todos los refresh tokens", description = "Revoca todos los refresh tokens del usuario autenticado",
        responses = {@ApiResponse(responseCode = "200", description = "Revocado", content = @Content)})
    @PostMapping("/revoke-all")
    public ResponseEntity<?> revokeAll(org.springframework.security.core.Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        return authService.revokeAllRefreshTokensForUser(userId);
    }

    @Operation(summary = "Obtener info del usuario actual", description = "Devuelve datos del usuario autenticado",
        responses = {@ApiResponse(responseCode = "200", description = "Usuario actual", content = @Content)})
    @GetMapping("/me")
    public ResponseEntity<?> me(org.springframework.security.core.Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        return authService.getCurrentUserById(userId);
    }
}
