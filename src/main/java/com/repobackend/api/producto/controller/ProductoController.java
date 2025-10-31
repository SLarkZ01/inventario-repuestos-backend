package com.repobackend.api.producto.controller;

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
import com.repobackend.api.producto.service.ProductoService;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/productos")
@Tag(name = "Productos", description = "Operaciones para gestionar productos")
public class ProductoController {
    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }

    @Operation(
        summary = "Crear producto",
        description = "Crea un nuevo producto en el inventario",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del producto",
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de producto",
                    value = "{\"nombre\":\"Filtro de Aceite Yamaha\",\"descripcion\":\"Filtro de aceite para motos Yamaha 150cc\",\"precio\":25.50,\"stock\":100,\"categoriaId\":\"507f1f77bcf86cd799439011\",\"specs\":{\"Marca\":\"Yamaha\",\"Modelo\":\"YZF-R15\",\"Compatibilidad\":\"150cc\"}}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha\",\"precio\":25.50,\"stock\":100}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PostMapping
    public ResponseEntity<?> crearProducto(@jakarta.validation.Valid @RequestBody ProductoRequest body) {
        var r = productoService.crearProducto(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @Operation(
        summary = "Listar productos",
        description = "Devuelve una lista paginada de productos. Soporta búsqueda por nombre (q) o filtrado por categoría (categoriaId).",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "q", description = "Búsqueda por nombre de producto", example = "filtro"),
            @io.swagger.v3.oas.annotations.Parameter(name = "categoriaId", description = "ID de la categoría para filtrar", example = "507f1f77bcf86cd799439011"),
            @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Número de página (base 0)", example = "0"),
            @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Cantidad de elementos por página", example = "20")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de productos paginada",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"content\":[{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite\",\"precio\":25.50}],\"page\":0,\"size\":20,\"totalElements\":45,\"totalPages\":3}"
                    )
                )
            )
        }
    )
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

    @Operation(
        summary = "Obtener producto por ID",
        description = "Devuelve los detalles completos de un producto",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha\",\"precio\":25.50,\"stock\":100,\"categoriaId\":\"507f1f77bcf86cd799439011\"}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable String id) {
        var maybe = productoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        return ResponseEntity.ok(Map.of("producto", maybe.get()));
    }

    @Operation(
        summary = "Actualizar producto",
        description = "Actualiza los datos del producto. Envía solo los campos que deseas actualizar.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos actualizados del producto",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"nombre\":\"Filtro de Aceite Yamaha R15\",\"precio\":27.00,\"stock\":150}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha R15\",\"precio\":27.00}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarProducto(@PathVariable String id, @jakarta.validation.Valid @RequestBody ProductoRequest body) {
        var r = productoService.actualizarProducto(id, body);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(
        summary = "Ajustar stock de producto",
        description = "Ajusta el stock del producto sumando o restando una cantidad (delta). Usa valores positivos para aumentar y negativos para disminuir.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Delta de ajuste de stock",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(value = "{\"delta\":10}")
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"stock\":110}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @PatchMapping("/{id}/stock")
    public ResponseEntity<?> ajustarStock(@PathVariable String id, @RequestBody Map<String, Object> body) {
        Number deltaN = (Number) body.getOrDefault("delta", 0);
        int delta = deltaN == null ? 0 : deltaN.intValue();
        var r = productoService.ajustarStock(id, delta);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar producto", description = "Elimina un producto por ID",
        responses = {@ApiResponse(responseCode = "200", description = "Producto eliminado", content = @Content),
                     @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)})
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = productoService.eliminarProducto(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
