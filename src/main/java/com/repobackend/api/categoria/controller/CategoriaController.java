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
import com.repobackend.api.categoria.service.CategoriaService;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gestión de categorías de productos")
public class CategoriaController {
    private final CategoriaService categoriaService;

    public CategoriaController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(
        summary = "Crear categoría",
        description = "Crea una nueva categoría de productos",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la categoría",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de categoría",
                    value = "{\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"iconoRecurso\":2131230988}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\"}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> crearCategoria(@jakarta.validation.Valid @RequestBody CategoriaRequest body) {
        var r = categoriaService.crearCategoria(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @Operation(
        summary = "Buscar/listar categorías",
        description = "Busca categorías por nombre. Si no se proporciona 'q', devuelve lista vacía.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "q", description = "Término de búsqueda para nombre de categoría", example = "filtro"),
            @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Número de página", example = "0"),
            @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Elementos por página", example = "20")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"content\":[{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\"}],\"totalElements\":1}")
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String q,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "20") int size) {
        if (q != null && !q.isBlank()) {
            var res = categoriaService.buscarPorNombrePaginado(q, page, size);
            return ResponseEntity.ok(res);
        }
        return ResponseEntity.ok(Map.of("categorias", List.of()));
    }

    @Operation(summary = "Obtener categoría", responses = {@ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content),
        @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoria(@PathVariable String id) {
        var maybe = categoriaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Categoria no encontrada"));
        return ResponseEntity.ok(Map.of("categoria", maybe.get()));
    }

    @Operation(summary = "Actualizar categoría", responses = {@ApiResponse(responseCode = "200", description = "Categoría actualizada", content = @Content),
        @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)})
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable String id, @jakarta.validation.Valid @RequestBody CategoriaRequest body) {
        var r = categoriaService.actualizarCategoria(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar categoría", responses = {@ApiResponse(responseCode = "200", description = "Eliminada", content = @Content),
        @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = categoriaService.eliminarCategoria(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
