package com.repobackend.api.producto.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.producto.dto.ProductoRequest;
import com.repobackend.api.producto.model.Producto;
import com.repobackend.api.producto.service.ProductoService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @PostMapping
    public ResponseEntity<?> crearProducto(@jakarta.validation.Valid @RequestBody ProductoRequest body, HttpServletRequest req) {
        var r = productoService.crearProducto(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String categoriaId, @RequestParam(required = false) String q,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "20") int size) {
        if (q != null && !q.isBlank()) {
            // usar paginación en búsqueda
            var pg = productoService.productosPorNombrePaginado(q, page, size);
            return ResponseEntity.ok(pg);
        }
        if (categoriaId != null) {
            var pg = productoService.listarPorCategoriaPaginado(categoriaId, page, size);
            return ResponseEntity.ok(pg);
        }
        // fallback: paginated list
        var pg = productoService.listar(page, size);
        return ResponseEntity.ok(pg);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable String id) {
        var maybe = productoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        return ResponseEntity.ok(Map.of("producto", maybe.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable String id, @jakarta.validation.Valid @RequestBody ProductoRequest body) {
        var r = productoService.actualizarProducto(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Number deltaN = (Number) body.getOrDefault("delta", 0);
        int delta = deltaN == null ? 0 : deltaN.intValue();
        var r = productoService.ajustarStock(id, delta);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = productoService.eliminarProducto(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
