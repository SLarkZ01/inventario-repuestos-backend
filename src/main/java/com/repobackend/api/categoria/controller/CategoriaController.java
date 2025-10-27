package com.repobackend.api.categoria.controller;

import java.util.List;
import java.util.Map;

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

import com.repobackend.api.categoria.dto.CategoriaRequest;
import com.repobackend.api.categoria.model.Categoria;
import com.repobackend.api.categoria.service.CategoriaService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/categorias")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @PostMapping
    public ResponseEntity<?> crearCategoria(@jakarta.validation.Valid @RequestBody CategoriaRequest body, HttpServletRequest req) {
        var r = categoriaService.crearCategoria(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String q) {
        if (q != null && !q.isBlank()) {
            List<Categoria> res = categoriaService.buscarPorNombre(q);
            return ResponseEntity.ok(Map.of("categorias", res));
        }
        return ResponseEntity.ok(Map.of("categorias", List.of()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoria(@PathVariable String id) {
        var maybe = categoriaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Categoria no encontrada"));
        return ResponseEntity.ok(Map.of("categoria", maybe.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable String id, @jakarta.validation.Valid @RequestBody CategoriaRequest body) {
        var r = categoriaService.actualizarCategoria(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = categoriaService.eliminarCategoria(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
