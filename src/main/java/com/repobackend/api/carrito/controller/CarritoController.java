package com.repobackend.api.carrito.controller;

import java.util.Map;
import java.util.stream.Collectors;

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

import com.repobackend.api.carrito.dto.CarritoItemRequest;
import com.repobackend.api.carrito.dto.CarritoRequest;
import com.repobackend.api.carrito.dto.CarritoResponse;
import com.repobackend.api.carrito.service.CarritoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/carritos")
public class CarritoController {
    private final CarritoService carritoService;

    public CarritoController(CarritoService carritoService) {
        this.carritoService = carritoService;
    }

    @PostMapping
    public ResponseEntity<?> crear(@Valid @RequestBody CarritoRequest body) {
        try {
            CarritoResponse r = carritoService.crearCarrito(body);
            return ResponseEntity.status(201).body(Map.of("carrito", r));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        }
    }

    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String usuarioId) {
        if (usuarioId == null) return ResponseEntity.ok(Map.of("carritos", java.util.List.of()));
        var res = carritoService.listarPorUsuario(usuarioId).stream().map(carritoService::toResponse).collect(Collectors.toList());
        return ResponseEntity.ok(Map.of("carritos", res));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getById(@PathVariable String id) {
        var maybe = carritoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Carrito no encontrado"));
        return ResponseEntity.ok(Map.of("carrito", carritoService.toResponse(maybe.get())));
    }

    @PostMapping("/{id}/items")
    public ResponseEntity<?> addItem(@PathVariable String id, @Valid @RequestBody CarritoItemRequest body, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        // Optionally enforce that carrito.usuarioId equals authenticated user, but here we simply require auth
        var r = carritoService.addItem(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}/items/{productoId}")
    public ResponseEntity<?> removeItem(@PathVariable String id, @PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = carritoService.removeItem(id, productoId);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @PostMapping("/{id}/clear")
    public ResponseEntity<?> clear(@PathVariable String id, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = carritoService.clear(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable String id, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        var r = carritoService.delete(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
