package com.repobackend.api.carrito.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.repobackend.api.carrito.dto.CarritoItemRequest;
import com.repobackend.api.carrito.dto.CarritoRequest;
import com.repobackend.api.carrito.dto.CarritoResponse;
import com.repobackend.api.carrito.service.CarritoService;

import jakarta.validation.Valid;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/carritos")
@Tag(name = "Carritos", description = "Operaciones sobre carritos de compra")
public class CarritoController {
    private final CarritoService carritoService;
    private final ObjectMapper objectMapper;

    public CarritoController(CarritoService carritoService, ObjectMapper objectMapper) {
        this.carritoService = carritoService;
        this.objectMapper = objectMapper;
    }

    @Operation(
        summary = "Crear carrito",
        description = "Crea un carrito nuevo para un usuario o anónimo. Si se omite usuarioId, crea un carrito anónimo que puede sincronizarse después del login.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del carrito",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Carrito con items",
                        value = "{\"usuarioId\":\"507f1f77bcf86cd799439011\",\"items\":[{\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":2,\"precioUnitario\":25.50}]}"
                    ),
                    @ExampleObject(
                        name = "Carrito anónimo vacío",
                        value = "{\"items\":[]}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Carrito creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"carrito\":{\"id\":\"507f1f77bcf86cd799439999\",\"usuarioId\":\"507f1f77bcf86cd799439011\",\"items\":[],\"total\":0.0}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CarritoRequest body) {
        try {
            CarritoResponse r = carritoService.crearCarrito(body);
            return ResponseEntity.status(201).body(Map.of("carrito", r));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        }
    }

    @Operation(summary = "Listar carritos por usuario", description = "Devuelve los carritos asociados a un usuario",
        responses = {@ApiResponse(responseCode = "200", description = "Lista de carritos", content = @Content)})
    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String usuarioId) {
        if (usuarioId == null) return ResponseEntity.ok(Map.of("carritos", java.util.List.of()));
        var res = carritoService.listarPorUsuario(usuarioId).stream().map(carritoService::toResponse).toList();
        return ResponseEntity.ok(Map.of("carritos", res));
    }

    @Operation(summary = "Obtener carrito por ID", description = "Devuelve un carrito por su id",
        responses = {@ApiResponse(responseCode = "200", description = "Carrito encontrado", content = @Content),
                     @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        return ResponseEntity.ok(Map.of("carrito", carritoService.toResponse(maybe.get())));
    }

    @Operation(
        summary = "Merge de carrito anónimo",
        description = "Sincroniza un carrito anónimo al usuario autenticado tras el login. Combina items del carrito anónimo con el carrito del usuario.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "ID del carrito anónimo y/o items a sincronizar",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Merge de carrito",
                    value = "{\"anonCartId\":\"507f1f77bcf86cd799439888\",\"items\":[{\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":3,\"precioUnitario\":25.50}]}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Merge realizado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"merged\":true,\"carritoId\":\"507f1f77bcf86cd799439999\",\"totalItems\":5}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @PostMapping("/merge")
    public ResponseEntity<?> merge(@RequestBody Map<String, Object> body, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        String anonCartId = body.get("anonCartId") == null ? null : String.valueOf(body.get("anonCartId"));
        java.util.List<CarritoItemRequest> items = null;
        if (body.get("items") != null) {
            items = objectMapper.convertValue(body.get("items"), objectMapper.getTypeFactory().constructCollectionType(java.util.List.class, CarritoItemRequest.class));
        }
        var resp = carritoService.mergeAnonymousCartIntoUser(anonCartId, items, userId);
        if (resp.containsKey("error")) return ResponseEntity.badRequest().body(resp);
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Agregar item a carrito",
        description = "Agrega un producto al carrito del usuario autenticado. Si el producto ya existe, actualiza la cantidad.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del carrito", required = true, example = "507f1f77bcf86cd799439999")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Item a agregar",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Item de carrito",
                    value = "{\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":2,\"precioUnitario\":25.50}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Item agregado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"carrito\":{\"id\":\"507f1f77bcf86cd799439999\",\"items\":[{\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":2}],\"total\":51.0}}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para modificar este carrito", content = @Content),
            @ApiResponse(responseCode = "404", description = "Carrito no encontrado", content = @Content)
        }
    )
    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItem(@PathVariable String id, @Valid @RequestBody CarritoItemRequest body, Authentication authentication) {
        // Permitir carritos anónimos: userId puede ser null
        String userId = authentication == null ? null : authentication.getName();
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        var carrito = maybe.get();
        String ownerId = carrito.getUsuarioId() == null ? null : carrito.getUsuarioId().toHexString();
        // Si el carrito tiene dueño Y el usuario actual no es el dueño -> 403
        if (ownerId != null && !ownerId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permisos para modificar este carrito"));
        }
        // Permitir modificar si: 1) carrito anónimo (ownerId==null), o 2) usuario es el dueño
        var r = carritoService.addItem(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(400).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Remover item de carrito", description = "Remueve un item del carrito del usuario autenticado",
        responses = {@ApiResponse(responseCode = "200", description = "Item removido", content = @Content)})
    @DeleteMapping("/{id}/items/{productoId}")
    public ResponseEntity<?> removeItem(@PathVariable String id, @PathVariable String productoId, Authentication authentication) {
        // Permitir carritos anónimos: userId puede ser null
        String userId = authentication == null ? null : authentication.getName();
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        var carrito = maybe.get();
        String ownerId = carrito.getUsuarioId() == null ? null : carrito.getUsuarioId().toHexString();
        // Si el carrito tiene dueño Y el usuario actual no es el dueño -> 403
        if (ownerId != null && !ownerId.equals(userId)) {
            return ResponseEntity.status(403).body(Map.of("error", "No tienes permisos para modificar este carrito"));
        }
        // Permitir modificar si: 1) carrito anónimo (ownerId==null), o 2) usuario es el dueño
        var r = carritoService.removeItem(id, productoId);
        if (r.containsKey("error")) return ResponseEntity.status(400).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Vaciar carrito", description = "Elimina todos los items del carrito",
        responses = {@ApiResponse(responseCode = "200", description = "Carrito vaciado", content = @Content)})
    @PostMapping("/{id}/clear")
    public ResponseEntity<?> clear(@PathVariable String id, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        var carrito = maybe.get();
        String ownerId = carrito.getUsuarioId() == null ? null : carrito.getUsuarioId().toHexString();
        if (ownerId == null || !ownerId.equals(userId)) return ResponseEntity.status(403).body(Map.of("error", "No tienes permisos para modificar este carrito"));
        var r = carritoService.clear(id);
        if (r.containsKey("error")) return ResponseEntity.status(400).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar carrito", description = "Elimina un carrito por id",
        responses = {@ApiResponse(responseCode = "200", description = "Carrito eliminado", content = @Content)})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        var carrito = maybe.get();
        String ownerId = carrito.getUsuarioId() == null ? null : carrito.getUsuarioId().toHexString();
        if (ownerId == null || !ownerId.equals(userId)) return ResponseEntity.status(403).body(Map.of("error", "No tienes permisos para modificar este carrito"));
        var r = carritoService.delete(id);
        if (r.containsKey("error")) return ResponseEntity.status(400).body(r);
        return ResponseEntity.ok(r);
    }
}