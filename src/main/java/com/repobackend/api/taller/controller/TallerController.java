package com.repobackend.api.taller.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.taller.service.TallerService;

import jakarta.servlet.http.HttpServletRequest;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/talleres")
@Tag(name = "Talleres", description = "Administración de talleres, almacenes e invitaciones")
public class TallerController {
    private final TallerService tallerService;

    public TallerController(TallerService tallerService) {
        this.tallerService = tallerService;
    }

    @Operation(
        summary = "Crear taller",
        description = "Crea un taller y lo asocia al usuario autenticado como propietario (owner)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del taller",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Nuevo taller",
                    value = "{\"nombre\":\"Taller Motos del Norte\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Taller creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"taller\":{\"id\":\"507f1f77bcf86cd799439777\",\"nombre\":\"Taller Motos del Norte\",\"ownerId\":\"507f1f77bcf86cd799439011\"}}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> crearTaller(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String nombre = body.get("nombre");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearTaller(userId, nombre);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @Operation(
        summary = "Crear almacén en taller",
        description = "Crea un almacén dentro de un taller existente. Solo el owner o administradores del taller pueden crear almacenes.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller", required = true, example = "507f1f77bcf86cd799439777")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del almacén",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Nuevo almacén",
                    value = "{\"nombre\":\"Almacén Principal\",\"ubicacion\":\"Calle 123, Local 5\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Almacén creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"almacen\":{\"id\":\"507faaa1bcf86cd799439011\",\"nombre\":\"Almacén Principal\",\"ubicacion\":\"Calle 123, Local 5\",\"tallerId\":\"507f1f77bcf86cd799439777\"}}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @PostMapping("/{tallerId}/almacenes")
    public ResponseEntity<?> crearAlmacen(@PathVariable String tallerId, @RequestBody Map<String, String> body, HttpServletRequest req) {
        String nombre = body.get("nombre");
        String ubicacion = body.get("ubicacion");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearAlmacen(userId, tallerId, nombre, ubicacion);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @Operation(
        summary = "Crear invitación por código",
        description = "Genera un código de invitación para que usuarios puedan unirse a un taller. Roles disponibles: VENDEDOR, ADMIN, MECANICO.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller", required = true, example = "507f1f77bcf86cd799439777")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Configuración de la invitación",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Invitación de vendedor",
                        value = "{\"role\":\"VENDEDOR\",\"daysValid\":7}"
                    ),
                    @ExampleObject(
                        name = "Invitación de admin",
                        value = "{\"role\":\"ADMIN\",\"daysValid\":30}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Invitación creada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"invitacion\":{\"code\":\"TALLER-ABC123XYZ\",\"role\":\"VENDEDOR\",\"expiresAt\":\"2024-11-06T10:30:00Z\"}}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @PostMapping("/{tallerId}/invitaciones/codigo")
    public ResponseEntity<?> crearInvitacionCodigo(@PathVariable String tallerId, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        String role = (String) body.getOrDefault("role", "VENDEDOR");
        int days = body.get("daysValid") == null ? 7 : ((Number) body.get("daysValid")).intValue();
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearInvitacionCodigo(userId, tallerId, role, days);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @Operation(
        summary = "Aceptar invitación por código",
        description = "Permite a un usuario autenticado unirse a un taller usando un código de invitación válido",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Código de invitación",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Aceptar invitación",
                    value = "{\"code\":\"TALLER-ABC123XYZ\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Invitación aceptada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"tallerId\":\"507f1f77bcf86cd799439777\",\"role\":\"VENDEDOR\",\"message\":\"Te has unido al taller exitosamente\"}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Código inválido o expirado", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @PostMapping("/invitaciones/accept")
    public ResponseEntity<?> acceptInvitacion(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String code = body.get("code");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.acceptInvitationByCode(userId, code);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.ok(r);
    }

    // Optional: list talleres of authenticated user
    @Operation(summary = "Listar talleres propios", description = "Lista los talleres donde el usuario es owner",
        responses = {@ApiResponse(responseCode = "200", description = "Lista de talleres", content = @Content)})
    @GetMapping
    public ResponseEntity<?> listMyTalleres(HttpServletRequest req) {
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        return ResponseEntity.ok(Map.of("talleres", tallerService.getTalleresByOwner(userId)));
    }
}
