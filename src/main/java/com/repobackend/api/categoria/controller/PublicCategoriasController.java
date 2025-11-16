package com.repobackend.api.categoria.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.categoria.service.CategoriaService;

import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controlador público para categorías (no requiere autenticación).
 * Permite a clientes móviles y públicos acceder a las categorías sin token.
 */
@RestController
@RequestMapping("/api/public/categorias")
@Tag(name = "PublicCategoriasController", description = "Endpoints públicos de categorías (sin autenticación)")
public class PublicCategoriasController {
    private static final Logger logger = LoggerFactory.getLogger(PublicCategoriasController.class);
    private final CategoriaService categoriaService;

    public PublicCategoriasController(CategoriaService categoriaService) {
        this.categoriaService = categoriaService;
    }

    @Operation(
        summary = "Listar todas las categorías (público)",
        description = """
            Devuelve todas las categorías (globales + por taller) sin requerir autenticación.
            Este endpoint está diseñado para clientes móviles y aplicaciones públicas.
            
            **Nota:** No requiere token de autenticación.
            """,
        security = {},  // Marcar como público
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Número de página", example = "0"),
            @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Elementos por página", example = "20"),
            @io.swagger.v3.oas.annotations.Parameter(name = "q", description = "Búsqueda por nombre (opcional)", example = "filtros")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categorias\":[{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"publicId\":\"products/507f1f77/abc123\",\"secure_url\":\"https://res.cloudinary.com/...\"}]}],\"total\":1,\"page\":0,\"size\":20}")
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> listarCategorias(
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size,
            @RequestParam(required = false) String q) {

        logger.info("GET /api/public/categorias called with page={} size={} q='{}'", page, size, q);

        // Si hay búsqueda por nombre
        if (q != null && !q.isBlank()) {
            var res = categoriaService.buscarPorNombrePaginado(q, page, size);
            return ResponseEntity.ok(res);
        }

        // Devolver todas las categorías
        return ResponseEntity.ok(categoriaService.listarTodasLasCategorias(page, size));
    }

    @Operation(
        summary = "Obtener categoría por ID (público)",
        description = """
            Devuelve los detalles de una categoría específica sin requerir autenticación.
            
            **Nota:** No requiere token de autenticación.
            """,
        security = {},
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID de la categoría", required = true, example = "507f1f77bcf86cd799439011")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/categoria-filtros-abc123\",\"secure_url\":\"https://res.cloudinary.com/...\"}]}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoria(@PathVariable String id) {
        logger.info("GET /api/public/categorias/{} called", id);

        var maybe = categoriaService.getById(id);
        if (maybe.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of("error", "Categoría no encontrada"));
        }

        return ResponseEntity.ok(Map.of("categoria", maybe.get()));
    }
}

