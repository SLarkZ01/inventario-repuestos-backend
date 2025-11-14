package com.repobackend.api.taller.controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.taller.service.ITallerService;
import com.repobackend.api.taller.dto.TallerRequest;
import com.repobackend.api.taller.dto.AlmacenRequest;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

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
    private static final Logger logger = LoggerFactory.getLogger(TallerController.class);
    private final ITallerService tallerService;

    public TallerController(ITallerService tallerService) {
        this.tallerService = tallerService;
    }

    @Operation(summary = "Crear taller",
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
    public ResponseEntity<?> crearTaller(@Valid @RequestBody TallerRequest body, HttpServletRequest req) {
        String nombre = body.getNombre();
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        logger.info("Enviando datos del taller: principal='{}' nombre='{}'", userId, nombre);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearTaller(userId, nombre);
        logger.info("Resultado crearTaller para user={}: {}", userId, r);
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
        description = "Genera un código de invitación para que usuarios puedan unirse a un taller. Roles disponibles: VENDEDOR, ADMIN.",
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
        logger.info("Solicitud GET /api/talleres desde principal='{}'", userId);
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var list = tallerService.getTalleresForUser(userId);
        logger.info("Datos de talleres recibidos para user='{}': count={}", userId, list == null ? 0 : list.size());
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Obtener taller",
        description = "Obtiene información del taller por id",
        responses = {@ApiResponse(responseCode = "200", description = "Taller encontrado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @GetMapping("/{tallerId}")
    public ResponseEntity<?> getTaller(@PathVariable String tallerId) {
        var maybe = tallerService.getTallerById(tallerId);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Taller no encontrado"));
        return ResponseEntity.ok(Map.of("taller", maybe.get()));
    }

    @Operation(summary = "Actualizar taller",
        description = "Actualiza datos del taller (solo nombre por ahora)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos a actualizar",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"nombre\":\"Nuevo Nombre\"}"))
        ),
        responses = {@ApiResponse(responseCode = "200", description = "Taller actualizado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes", content = @Content)})
    @PutMapping("/{tallerId}")
    public ResponseEntity<?> actualizarTaller(@PathVariable String tallerId, @Valid @RequestBody TallerRequest body, HttpServletRequest req) {
        String callerId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (callerId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = tallerService.actualizarTaller(callerId, tallerId, body.getNombre());
        if (r.containsKey("error")) {
            int status = r.get("status") == null ? 400 : (int) r.get("status");
            return ResponseEntity.status(status).body(r);
        }
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar taller",
        description = "Elimina permanentemente el taller y todos sus almacenes asociados. Solo el owner puede hacerlo. Esta acción no se puede deshacer.",
        responses = {@ApiResponse(responseCode = "200", description = "Taller eliminado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @DeleteMapping("/{tallerId}")
    public ResponseEntity<?> eliminarTaller(@PathVariable String tallerId, HttpServletRequest req) {
        String callerId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (callerId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = tallerService.deleteTaller(callerId, tallerId);
        if (r.containsKey("error")) {
            int status = r.get("status") == null ? 400 : (int) r.get("status");
            return ResponseEntity.status(status).body(r);
        }
        return ResponseEntity.ok(r);
    }

    // Almacenes: listar por taller
    @Operation(summary = "Listar almacenes de un taller",
        description = "Devuelve los almacenes asociados a un taller",
        responses = {@ApiResponse(responseCode = "200", description = "Lista de almacenes", content = @Content),
            @ApiResponse(responseCode = "404", description = "Taller no encontrado", content = @Content)})
    @GetMapping("/{tallerId}/almacenes")
    public ResponseEntity<?> listarAlmacenes(@PathVariable String tallerId) {
        var r = tallerService.listAlmacenesByTaller(tallerId);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        // Devolver directamente el array de almacenes (no envuelto en objeto)
        return ResponseEntity.ok(r.get("almacenes"));
    }

    @Operation(summary = "Obtener almacén",
        description = "Obtiene un almacén por id",
        responses = {@ApiResponse(responseCode = "200", description = "Almacén encontrado", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @GetMapping("/{tallerId}/almacenes/{almacenId}")
    public ResponseEntity<?> getAlmacen(@PathVariable String tallerId, @PathVariable String almacenId) {
        var maybe = tallerService.findAlmacenById(almacenId);
        if (maybe.isEmpty() || !maybe.get().getTallerId().equals(tallerId)) return ResponseEntity.status(404).body(Map.of("error", "Almacén no encontrado"));
        return ResponseEntity.ok(Map.of("almacen", maybe.get()));
    }

    @Operation(summary = "Actualizar almacén",
        description = "Actualiza un almacén (nombre/ubicacion). Requiere owner o ADMIN.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de almacén",
            content = @Content(mediaType = "application/json", examples = @ExampleObject(value = "{\"nombre\":\"Almacén Nuevo\",\"ubicacion\":\"Calle 1\"}"))
        ),
        responses = {@ApiResponse(responseCode = "200", description = "Almacén actualizado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @PutMapping("/{tallerId}/almacenes/{almacenId}")
    public ResponseEntity<?> actualizarAlmacen(@PathVariable String tallerId, @PathVariable String almacenId, @Valid @RequestBody AlmacenRequest body, HttpServletRequest req) {
        String callerId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (callerId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = tallerService.updateAlmacen(callerId, tallerId, almacenId, body.getNombre(), body.getUbicacion());
        if (r.containsKey("error")) {
            int status = r.get("status") == null ? 400 : (int) r.get("status");
            return ResponseEntity.status(status).body(r);
        }
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar almacén",
        description = "Elimina permanentemente un almacén. Requiere owner o ADMIN. Esta acción no se puede deshacer.",
        responses = {@ApiResponse(responseCode = "200", description = "Almacén eliminado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes", content = @Content),
            @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @DeleteMapping("/{tallerId}/almacenes/{almacenId}")
    public ResponseEntity<?> eliminarAlmacen(@PathVariable String tallerId, @PathVariable String almacenId, HttpServletRequest req) {
        String callerId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (callerId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = tallerService.deleteAlmacen(callerId, tallerId, almacenId);
        if (r.containsKey("error")) {
            int status = r.get("status") == null ? 400 : (int) r.get("status");
            return ResponseEntity.status(status).body(r);
        }
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Listar miembros del taller",
        description = "Devuelve los miembros de un taller (userId, roles, joinedAt). Requiere que el usuario autenticado pertenezca al taller.",
        responses = {@ApiResponse(responseCode = "200", description = "Lista de miembros", content = @Content),
            @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No eres miembro del taller", content = @Content),
            @ApiResponse(responseCode = "404", description = "Taller no encontrado", content = @Content)})
    @GetMapping("/{tallerId}/miembros")
    public ResponseEntity<?> listarMiembros(@PathVariable String tallerId, HttpServletRequest req) {
        String callerId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (callerId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        // verificar que el usuario pertenece al taller
        if (!tallerService.isUserMember(callerId, tallerId)) return ResponseEntity.status(403).body(Map.of("error", "No eres miembro del taller"));
        var r = tallerService.listMembers(tallerId);
        if (r.containsKey("error")) {
            int status = r.get("status") == null ? 404 : (int) r.get("status");
            return ResponseEntity.status(status).body(r);
        }
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "DEBUG: listar talleres por userId (temporal)", description = "Endpoint temporal para depuración: pasar ?userId=<id> para simular autenticación y devolver talleres para ese userId. Eliminar/proteger en producción.", responses = {@ApiResponse(responseCode = "200", description = "Debug info", content = @Content)})
    @GetMapping("/_debug")
    public ResponseEntity<?> debugListByUser(@RequestParam(required = false) String userId, HttpServletRequest req) {
        // Si no se pasa userId, intentar tomar el principal
        String principal = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        String effective = userId == null ? principal : userId;
        if (effective == null) return ResponseEntity.ok(Map.of("error", "Necesitas pasar userId en query o estar autenticado"));
        var list = tallerService.getTalleresForUser(effective);
        logger.warn("DEBUG /api/talleres/_debug called. effectiveUser='{}' resultCount={}", effective, list == null ? 0 : list.size());
        return ResponseEntity.ok(Map.of("effectiveUser", effective, "count", list == null ? 0 : list.size(), "talleres", list));
    }
}
