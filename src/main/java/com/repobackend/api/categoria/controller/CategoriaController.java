package com.repobackend.api.categoria.controller;

import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
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
import com.repobackend.api.auth.service.AuthorizationService;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/categorias")
@Tag(name = "Categorias", description = "Gestión de categorías de productos")
public class CategoriaController {
    private static final Logger logger = LoggerFactory.getLogger(CategoriaController.class);
    private final CategoriaService categoriaService;
    private final AuthorizationService authorizationService;

    @Value("${app.default.taller-id:}")
    private String defaultTallerId;

    public CategoriaController(CategoriaService categoriaService, AuthorizationService authorizationService) {
        this.categoriaService = categoriaService;
        this.authorizationService = authorizationService;
    }

    @Operation(
        summary = "Crear categoría",
        description = "Crea una nueva categoría de productos. Nota: `tallerId` es obligatorio ya que todas las categorías pertenecen a un taller.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos de la categoría (tallerId obligatorio). `listaMedios` acepta una lista de objetos con campos: type, publicId, secure_url, format, order.",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de categoría",
                    value = "{\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"iconoRecurso\":2131230988,\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image\",\"publicId\":\"products/507f1f77/abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1/products/abc123.jpg\",\"format\":\"jpg\",\"order\":0}]}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"publicId\":\"products/507f1f77/abc123\",\"secure_url\":\"https://res.cloudinary.com/...\"}]}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (p.ej. falta tallerId)", content = @Content)
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
        description = "Busca categorías por nombre o lista categorías de un taller. Por defecto `tallerId` es obligatorio; usar `todas=true` sólo si se es platform-admin para obtener todas las categorías.",
        // Indicar que esta operación es pública (no requiere seguridad) para que los clientes Android puedan consumirla sin token
        security = {},
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "q", description = "Término de búsqueda para nombre de categoría", example = "filtro"),
            @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Número de página", example = "0"),
            @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Elementos por página", example = "20"),
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller (obligatorio para listar)", example = "507f1f77bcf86cd799439777", required = true),
            @io.swagger.v3.oas.annotations.Parameter(name = "todas", description = "Si true y el caller es platform-admin devuelve todas las categorías", example = "false")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de categorías",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categorias\":[{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"publicId\":\"products/507f1f77/abc123\",\"secure_url\":\"https://res.cloudinary.com/...\"}]}],\"total\":1}")
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String q,
                                    @RequestParam(required = false, defaultValue = "0") int page,
                                    @RequestParam(required = false, defaultValue = "20") int size,
                                    @RequestParam(required = false) String tallerId,
                                    @RequestParam(required = false, defaultValue = "false") boolean todas) {
        logger.info("GET /api/categorias called with q='{}' tallerId='{}' page={} size={} todas={}", q, tallerId, page, size, todas);
        // búsqueda por nombre
        if (q != null && !q.isBlank()) {
            var res = categoriaService.buscarPorNombrePaginado(q, page, size);
            return ResponseEntity.ok(res);
        }
        // si piden todas, sólo platform admin puede
        if (todas) {
            org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            String caller = auth == null ? null : auth.getName();
            // Si es platform-admin devolvemos todas las categorías (globales + talleres).
            if (authorizationService.isPlatformAdmin(caller)) {
                return ResponseEntity.ok(categoriaService.listarTodasLasCategorias(page, size));
            }
            // Para callers no-admin (incluye anónimos) devolvemos únicamente las categorías globales
            // para que clientes públicos (apps móviles) puedan mostrar categorías sin autenticación.
            logger.info("Non-admin caller '{}' requested todas=true — returning global categories instead of denying access", caller);
            return ResponseEntity.ok(categoriaService.listarCategoriasGlobales(page, size));
        }
        // Por defecto, requerimos tallerId para listar categorías (las categorías pertenecen a talleres)
        if (tallerId == null || tallerId.isBlank()) {
            // Si no se proporcionó tallerId y no hay término de búsqueda ni flag 'todas',
            // devolvemos las categorías globales para permitir que clientes públicos
            // vean categorías tipo catálogo (comportamiento tipo marketplace/public).
            logger.info("No tallerId provided — returning global categories (public) for listing");
            var globals = categoriaService.listarCategoriasGlobales(page, size);
            // si no hay categorias globales y se configuró un defaultTallerId, usamos ese taller
            java.util.List<?> cats = (java.util.List<?>) globals.getOrDefault("categorias", java.util.List.of());
            if ((cats == null || cats.isEmpty()) && defaultTallerId != null && !defaultTallerId.isBlank()) {
                logger.info("Global categories empty — falling back to defaultTallerId='{}'", defaultTallerId);
                return ResponseEntity.ok(categoriaService.listarCategoriasPorTaller(defaultTallerId, page, size));
            }
            return ResponseEntity.ok(globals);
        }
        return ResponseEntity.ok(categoriaService.listarCategoriasPorTaller(tallerId, page, size));
    }

    @Operation(summary = "Obtener categoría",
        // marcar como pública en OpenAPI
        security = {},
        responses = {@ApiResponse(responseCode = "200", description = "Categoría encontrada", content = @Content),
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
