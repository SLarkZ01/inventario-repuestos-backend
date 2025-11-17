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
import io.swagger.v3.oas.annotations.media.Schema;

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
        description = """
            Crea un nuevo producto en el inventario.
            
            **NUEVO (v2.0): Campo tasaIva obligatorio para facturación**
            - `tasaIva`: Tasa de IVA en porcentaje (ej: 0, 5, 19). **Default: 19%** si no se especifica
            - Este campo es usado automáticamente al generar facturas para calcular el IVA
            - Valores comunes en Colombia: 0% (exento), 5% (canasta básica), 19% (estándar)
            
            **IMPORTANTE - Gestión de Imágenes:**
            - Las imágenes DEBEN subirse primero a Cloudinary usando `/api/uploads/cloudinary-sign`
            - Cada objeto en `listaMedios` DEBE incluir el campo `publicId` (CRÍTICO para eliminar imágenes al borrar el producto)
            - Sin `publicId`, las imágenes quedarán huérfanas en Cloudinary y no se podrán eliminar automáticamente
            
            **Campos recomendados:**
            - `tallerId`: Asocia el producto a un taller específico (recomendado para multi-tenant)
            - `specs`: Mapa de especificaciones técnicas (ej: {"Marca":"Yamaha", "Modelo":"R15", "Peso":"0.2kg"})
            - `listaMedios`: Array de objetos con estructura: {type, publicId, secure_url, format, width, height, order}
            
            **Flujo recomendado para imágenes:**
            1. Obtener firma: POST /api/uploads/cloudinary-sign con {folder: "products"}
            2. Subir a Cloudinary con la firma obtenida
            3. Guardar en `listaMedios` el objeto completo que devuelve Cloudinary (especialmente `public_id`)
            """,
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                Datos del producto. 
                **NUEVO**: Campo `tasaIva` (IVA en %) - Si no se envía, se asigna 19% por defecto.
                **CRÍTICO**: Si incluyes `listaMedios`, cada medio DEBE tener `publicId` para poder eliminar las imágenes de Cloudinary.
                El campo `specs` es opcional pero recomendado para especificaciones técnicas.
                """,
            required = true,
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Ejemplo de producto completo con IVA",
                    value = "{\"nombre\":\"Filtro de Aceite Yamaha\",\"descripcion\":\"Filtro de aceite para motos Yamaha 150cc\",\"precio\":25000,\"tasaIva\":19.0,\"stock\":100,\"categoriaId\":\"507f1f77bcf86cd799439011\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/filtro-yamaha-abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763285023/products/507f1f77/filtro-yamaha-abc123.jpg\",\"url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763285023/products/507f1f77/filtro-yamaha-abc123.jpg\",\"format\":\"jpg\",\"width\":800,\"height\":600,\"order\":0}],\"specs\":{\"Marca\":\"Yamaha\",\"Modelo\":\"YZF-R15\",\"Compatibilidad\":\"150cc\",\"Material\":\"Papel\",\"Peso\":\"0.2kg\"}}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Producto creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha\",\"precio\":25000,\"tasaIva\":19.0,\"stock\":100,\"tallerId\":\"507f1f77bcf86cd799439777\"}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos (ej: nombre vacío, stock negativo)", content = @Content)
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
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller/tienda para filtrar productos por tienda", example = "507f1f77bcf86cd799439777", schema = @Schema(type = "string")),
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
        description = """
            Devuelve los detalles completos de un producto.
            
            **La respuesta incluye:**
            - Datos básicos (nombre, descripción, precio, stock)
            - **tasaIva**: Tasa de IVA en porcentaje (usado para calcular IVA en facturas)
            - `listaMedios`: Array con imágenes (cada una con publicId, secure_url, etc.)
            - `specs`: Objeto con especificaciones técnicas (si existen)
            - `tallerId`: ID del taller propietario (si existe)
            - `categoriaId`: ID de la categoría (si existe)
            """,
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto encontrado",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha\",\"descripcion\":\"Filtro de aceite para motos Yamaha 150cc\",\"precio\":25000,\"tasaIva\":19.0,\"stock\":100,\"categoriaId\":\"507f1f77bcf86cd799439011\",\"tallerId\":\"507f1f77bcf86cd799439777\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/507f1f77/filtro-yamaha-abc123\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763285023/products/507f1f77/filtro-yamaha-abc123.jpg\",\"format\":\"jpg\",\"width\":800,\"height\":600,\"order\":0}],\"specs\":{\"Marca\":\"Yamaha\",\"Modelo\":\"YZF-R15\",\"Compatibilidad\":\"150cc\",\"Material\":\"Papel\",\"Peso\":\"0.2kg\"}}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @GetMapping("/{id}")
    public ResponseEntity<?> getProducto(@PathVariable String id) {
        var maybe = productoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Producto no encontrado"));
        var response = productoService.toResponse(maybe.get());
        return ResponseEntity.ok(Map.of("producto", response));
    }

    @Operation(
        summary = "Actualizar producto",
        description = """
            Actualiza los datos del producto. Envía solo los campos que deseas actualizar.
            
            **IMPORTANTE sobre `listaMedios`:**
            - Si actualizas `listaMedios`, DEBE incluir `publicId` en cada medio
            - Al actualizar, se reemplazan las imágenes anteriores (las viejas se eliminan de Cloudinary automáticamente)
            - Para agregar nuevas imágenes manteniendo las existentes, primero obtén el producto actual y agrega a su array
            
            **Nota sobre `specs`:**
            - Al actualizar `specs`, se reemplaza el objeto completo (no se hace merge)
            - Para actualizar una sola especificación, envía el objeto completo con el cambio
            """,
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = """
                Datos actualizados del producto (solo enviar los campos a modificar).
                Si actualizas `listaMedios`, DEBE incluir `publicId` en cada elemento.
                """,
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Actualizar datos básicos",
                        value = "{\"nombre\":\"Filtro de Aceite Yamaha R15\",\"precio\":27.00,\"stock\":150}"
                    ),
                    @ExampleObject(
                        name = "Actualizar con nueva imagen",
                        value = "{\"nombre\":\"Filtro de Aceite Yamaha R15\",\"listaMedios\":[{\"type\":\"image/jpeg\",\"publicId\":\"products/nuevo-filtro-xyz789\",\"secure_url\":\"https://res.cloudinary.com/df7ggzasi/image/upload/v1763285500/products/nuevo-filtro-xyz789.jpg\",\"format\":\"jpg\",\"order\":0}]}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto actualizado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"producto\":{\"id\":\"507f191e810c19729de860ea\",\"nombre\":\"Filtro de Aceite Yamaha R15\",\"precio\":27.00}}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
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

    @Operation(
        summary = "Eliminar producto",
        description = "Elimina un producto por ID. **IMPORTANTE**: También elimina automáticamente las imágenes asociadas de Cloudinary (si existen en `listaMedios`).",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "id", description = "ID del producto a eliminar", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Producto eliminado exitosamente (y sus medios de Cloudinary)",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"deleted\":true}")
                )
            ),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content),
            @ApiResponse(responseCode = "403", description = "Sin permisos para eliminar este producto", content = @Content)
        }
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminar(@PathVariable String id) {
        var r = productoService.eliminarProducto(id);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
