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
        description = """
            Crea una nueva categoría de productos.
            
            **IMPORTANTE:**
            - `tallerId` es REQUERIDO para crear categorías (validación en el servicio)
            - Si no se proporciona `tallerId`, el backend devolverá: {"error": "tallerId es obligatorio para crear una categoría"}
            
            **Gestión de Imágenes:**
            - Las imágenes DEBEN subirse primero a Cloudinary usando `/api/uploads/cloudinary-sign`
            - Cada objeto en `listaMedios` DEBE incluir el campo `publicId` (CRÍTICO para eliminar imágenes al borrar la categoría)
            - Sin `publicId`, las imágenes quedarán huérfanas en Cloudinary y no se podrán eliminar automáticamente
            
            **Estructura de `listaMedios`:**
            - Cada elemento debe tener: {type, publicId, secure_url, format, width, height, order}
            - El campo `publicId` es retornado por Cloudinary como `public_id` al subir la imagen
            
            **Flujo recomendado para imágenes:**
            1. Obtener firma: POST /api/uploads/cloudinary-sign con {folder: "products"}
            2. Subir a Cloudinary con la firma obtenida
            3. Guardar en `listaMedios` el objeto completo que devuelve Cloudinary (especialmente `public_id`)
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                Datos de la categoría.
                **REQUERIDO**: `nombre` y `tallerId` (el backend validará que `tallerId` esté presente)
                **CRÍTICO**: Si incluyes `listaMedios`, cada medio DEBE tener `publicId` para poder eliminar las imágenes de Cloudinary.
                """,
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de categoría completa",
                    value = "{\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/categoria-filtros-abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763282466/products/507f1f77/categoria-filtros-abc123.jpg\",\"url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763282466/products/507f1f77/categoria-filtros-abc123.jpg\",\"format\":\"jpg\",\"width\":800,\"height\":600,\"order\":0}],\"mappedGlobalCategoryId\":\"global-cat-filtros\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Categoría creada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/categoria-filtros-abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763282466/products/507f1f77/categoria-filtros-abc123.jpg\"}]}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej: falta tallerId, nombre vacío, o error de validación)",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\":\"tallerId es obligatorio para crear una categoría\"}")
                )
            ),
            @ApiResponse(responseCode = "403", description = "Permisos insuficientes",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"error\":\"Permisos insuficientes para crear categoría en este taller\"}")
                )
            )
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
            // COMPORTAMIENTO CAMBIADO: Para callers no-admin (incluye anónimos) devolvemos
            // también TODAS las categorías (globales + por taller) para que clientes
            // móviles vean el mismo catálogo que el Admin. Dejamos el log para trazabilidad.
            logger.info("Non-admin caller '{}' requested todas=true — returning ALL categories to public clients", caller);
            return ResponseEntity.ok(categoriaService.listarTodasLasCategorias(page, size));
        }
        // Por defecto, requerimos tallerId para listar categorías (las categorías pertenecen a talleres)
        if (tallerId == null || tallerId.isBlank()) {
            // COMPORTAMIENTO CAMBIADO: devolver todas las categorías (globales + locales)
            // para clientes públicos (Android) para que no importe si pertenecen a un taller.
            logger.info("No tallerId provided — returning ALL categories (global + by-taller) for public clients");
            return ResponseEntity.ok(categoriaService.listarTodasLasCategorias(page, size));
        }
        return ResponseEntity.ok(categoriaService.listarCategoriasPorTaller(tallerId, page, size));
    }

    @Operation(
        summary = "Obtener categoría por ID",
        description = """
            Devuelve los detalles completos de una categoría.
            
            **La respuesta incluye:**
            - Datos básicos (nombre, descripción)
            - `tallerId`: ID del taller propietario
            - `listaMedios`: Array con imágenes (cada una con publicId, secure_url, etc.)
            - `mappedGlobalCategoryId`: ID de categoría global (si existe)
            
            **Nota:** Este endpoint es público (no requiere autenticación)
            """,
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID de la categoría", required = true, example = "507f1f77bcf86cd799439011")
        },
        // marcar como pública en OpenAPI
        security = {},
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoría encontrada",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros\",\"descripcion\":\"Filtros de aceite, aire y combustible\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/categoria-filtros-abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763282466/products/507f1f77/categoria-filtros-abc123.jpg\",\"format\":\"jpg\",\"width\":800,\"height\":600,\"order\":0}],\"mappedGlobalCategoryId\":\"global-cat-filtros\"}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getCategoria(@PathVariable String id) {
        var maybe = categoriaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Categoria no encontrada"));
        return ResponseEntity.ok(Map.of("categoria", maybe.get()));
    }

    @Operation(
        summary = "Actualizar categoría",
        description = """
            Actualiza los datos de una categoría. Envía solo los campos que deseas actualizar.
            
            **IMPORTANTE sobre `listaMedios`:**
            - Si actualizas `listaMedios`, DEBE incluir `publicId` en cada medio
            - Al actualizar, se reemplazan las imágenes anteriores (las viejas se eliminan de Cloudinary automáticamente)
            - Para agregar nuevas imágenes manteniendo las existentes, primero obtén la categoría actual y agrega a su array
            
            **Campos actualizables:**
            - `nombre`: Nombre de la categoría
            - `descripcion`: Descripción
            - `tallerId`: ID del taller (normalmente no se cambia)
            - `listaMedios`: Array de medios (DEBE incluir publicId)
            - `mappedGlobalCategoryId`: ID de categoría global
            """,
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID de la categoría", required = true, example = "507f1f77bcf86cd799439011")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                Datos actualizados de la categoría (solo enviar los campos a modificar).
                Si actualizas `listaMedios`, DEBE incluir `publicId` en cada elemento.
                """,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Actualizar nombre y descripción",
                        value = "{\"nombre\":\"Filtros Premium\",\"descripcion\":\"Filtros de alta calidad para motos\"}"
                    ),
                    @ExampleObject(
                        name = "Actualizar con nueva imagen",
                        value = "{\"nombre\":\"Filtros Premium\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/nueva-categoria-xyz789\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763285500/products/nueva-categoria-xyz789.jpg\",\"format\":\"jpg\",\"order\":0}]}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoría actualizada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"categoria\":{\"id\":\"507f1f77bcf86cd799439011\",\"nombre\":\"Filtros Premium\",\"descripcion\":\"Filtros de alta calidad para motos\"}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarCategoria(@PathVariable String id, @jakarta.validation.Valid @RequestBody CategoriaRequest body) {
        var r = categoriaService.actualizarCategoria(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(
        summary = "Eliminar categoría",
        description = """
            Elimina una categoría por ID.
            
            **IMPORTANTE:** 
            - También elimina automáticamente las imágenes asociadas de Cloudinary (si existen en `listaMedios` con `publicId`)
            - Si la categoría tiene productos asociados, puede fallar (dependiendo de la lógica de negocio)
            - Esta acción no se puede deshacer
            
            **Limpieza automática:**
            - Todas las imágenes en `listaMedios` que tengan `publicId` serán eliminadas de Cloudinary
            - Si alguna imagen no tiene `publicId`, quedará huérfana en Cloudinary
            """,
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID de la categoría a eliminar", required = true, example = "507f1f77bcf86cd799439011")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Categoría eliminada exitosamente (y sus medios de Cloudinary)",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"deleted\":true,\"id\":\"507f1f77bcf86cd799439011\"}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Categoría no encontrada", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar esta categoría", content = @Content)
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = categoriaService.eliminarCategoria(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
