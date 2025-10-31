package com.repobackend.api.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.auth.dto.AdminRequests.CreateAdminRequest;
import com.repobackend.api.auth.dto.AdminRequests.CreateAdminResponse;
import com.repobackend.api.admin.service.AdminUserService;

import jakarta.validation.Valid;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/admin/users")
@Tag(name = "AdminUsers", description = "Creación de administradores")
public class AdminUserController {
    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @Operation(
        summary = "Crear administrador",
        description = "Crea un nuevo usuario con rol de administrador. Solo puede ser ejecutado por usuarios que ya son administradores.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del nuevo administrador",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Crear admin",
                    value = "{\"username\":\"admin.sistemas\",\"email\":\"admin@empresa.com\",\"password\":\"SecurePass123!\",\"nombre\":\"Juan\",\"apellido\":\"Administrador\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Administrador creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"userId\":\"507f1f77bcf86cd799439011\",\"username\":\"admin.sistemas\",\"email\":\"admin@empresa.com\",\"role\":\"ADMIN\",\"message\":\"Administrador creado exitosamente\"}"
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o usuario ya existe", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para crear administradores", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> createAdmin(@Valid @RequestBody CreateAdminRequest req, Authentication authentication) {
        // Determine caller privileges: if authentication != null and has ROLE_ADMIN
        boolean callerIsAdmin = false;
        String callerId = null;
        if (authentication != null && authentication.isAuthenticated()) {
            callerId = authentication.getName();
            callerIsAdmin = authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        }
        CreateAdminResponse resp = adminUserService.createAdmin(req, callerId, callerIsAdmin);
        return ResponseEntity.status(201).body(resp);
    }
}
