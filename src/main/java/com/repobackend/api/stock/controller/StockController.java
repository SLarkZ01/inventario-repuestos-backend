package com.repobackend.api.stock.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.stock.service.StockService;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/stock")
@Tag(name = "Stock", description = "Operaciones para gestionar inventario por almacén")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @Operation(
        summary = "Obtener stock por producto",
        description = "Devuelve el stock disponible de un producto desglosado por almacén y el total consolidado",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "productoId", description = "ID del producto", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock obtenido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"stockByAlmacen\":[{\"almacenId\":\"507faaa1bcf86cd799439011\",\"almacenNombre\":\"Principal\",\"cantidad\":50},{\"almacenId\":\"507fbbb1bcf86cd799439012\",\"almacenNombre\":\"Sucursal\",\"cantidad\":30}],\"total\":80}"
                    )
                )
            )
        }
    )
    @GetMapping
    public ResponseEntity<?> getByProducto(@RequestParam String productoId) {
        var rows = stockService.getStockByProducto(productoId);
        return ResponseEntity.ok(Map.of("stockByAlmacen", rows, "total", stockService.getTotalStock(productoId)));
    }

    @Operation(
        summary = "Ajustar stock (delta)",
        description = "Aumenta o disminuye el stock en un almacén específico. Use valores positivos para aumentar y negativos para disminuir.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Datos del ajuste",
            content = @Content(
                mediaType = "application/json",
                examples = {
                    @ExampleObject(
                        name = "Aumentar stock",
                        value = "{\"productoId\":\"507f191e810c19729de860ea\",\"almacenId\":\"507faaa1bcf86cd799439011\",\"delta\":50}"
                    ),
                    @ExampleObject(
                        name = "Disminuir stock",
                        value = "{\"productoId\":\"507f191e810c19729de860ea\",\"almacenId\":\"507faaa1bcf86cd799439011\",\"delta\":-10}"
                    )
                }
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock ajustado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"nuevaCantidad\":140}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PostMapping("/adjust")
    public ResponseEntity<?> adjust(@RequestBody Map<String, Object> body, Authentication authentication) {
        String productoId = (String) body.get("productoId");
        String almacenId = (String) body.get("almacenId");
        Number deltaN = (Number) body.getOrDefault("delta", 0);
        int delta = deltaN == null ? 0 : deltaN.intValue();
        String userId = authentication == null ? null : authentication.getName();
        var r = stockService.adjustStock(productoId, almacenId, delta, userId);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(
        summary = "Setear stock",
        description = "Establece la cantidad exacta de stock en un almacén específico (reemplaza el valor anterior)",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "Cantidad exacta a establecer",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Establecer stock",
                    value = "{\"productoId\":\"507f191e810c19729de860ea\",\"almacenId\":\"507faaa1bcf86cd799439011\",\"cantidad\":100}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "200", description = "Stock establecido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"cantidad\":100}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content)
        }
    )
    @PutMapping("/set")
    public ResponseEntity<?> set(@RequestBody Map<String, Object> body, Authentication authentication) {
        String productoId = (String) body.get("productoId");
        String almacenId = (String) body.get("almacenId");
        Number cantidadN = (Number) body.getOrDefault("cantidad", 0);
        int cantidad = cantidadN == null ? 0 : cantidadN.intValue();
        String userId = authentication == null ? null : authentication.getName();
        var r = stockService.setStock(productoId, almacenId, cantidad, userId);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.ok(r);
    }

    @Operation(summary = "Eliminar registro de stock", description = "Elimina el registro de stock para producto+almacén",
        responses = {@ApiResponse(responseCode = "200", description = "Registro eliminado", content = @Content),
                     @ApiResponse(responseCode = "404", description = "No encontrado", content = @Content)})
    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String productoId, @RequestParam String almacenId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = stockService.removeStockRecord(productoId, almacenId, userId);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
