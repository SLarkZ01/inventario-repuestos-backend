package com.repobackend.api.producto.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.producto.service.ProductoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@RestController
@RequestMapping("/api/public/productos")
@Tag(name = "PublicProductosController", description = "Endpoints públicos de productos (sin autenticación)")
public class PublicProductosController {
    private static final Logger logger = LoggerFactory.getLogger(PublicProductosController.class);

    private final ProductoService productoService;

    public PublicProductosController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(
        summary = "Listar productos (público)",
        description = """
            Devuelve productos paginados para clientes públicos (sin autenticación).
            
            Soporta:
            - Búsqueda por nombre con `q`
            - Filtrado por `categoriaId`
            - Paginación con `page` y `size`
            
            La respuesta expone `totalStock` agregado y `stockByAlmacen` cuando está disponible.
            """,
        security = {},
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de productos",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"productos\":[{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro\",\"precio\":25.5,\"totalStock\":50}],\"total\":1,\"page\":0,\"size\":20}")
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String categoriaId,
            @RequestParam(required = false, defaultValue = "0") int page,
            @RequestParam(required = false, defaultValue = "20") int size) {
        logger.info("GET /api/public/productos q='{}' categoriaId='{}' page={} size={}", q, categoriaId, page, size);
        if (q != null && !q.isBlank()) {
            return ResponseEntity.ok(productoService.productosPorNombrePaginado(q, page, size));
        }
        if (categoriaId != null && !categoriaId.isBlank()) {
            return ResponseEntity.ok(productoService.listarPorCategoriaPaginado(categoriaId, page, size));
        }
        return ResponseEntity.ok(productoService.listar(page, size));
    }

    @Operation(
        summary = "Obtener producto por ID (público)",
        description = """
            Devuelve los detalles completos de un producto con `totalStock` agregado y `stockByAlmacen`.
            
            Este endpoint no requiere autenticación.
            """,
        security = {},
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite\",\"precio\":25.5,\"totalStock\":50,\"stockByAlmacen\":[{\"almacenId\":\"abc\",\"cantidad\":20}]}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable String id) {
        var maybe = productoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(java.util.Map.of("error", "Producto no encontrado"));
        var resp = productoService.toResponse(maybe.get());
        return ResponseEntity.ok(java.util.Map.of("producto", resp));
    }
}

