package com.repobackend.api.factura.controller;

import java.util.List;
import java.util.Map;

import com.repobackend.api.factura.dto.FacturaRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.service.FacturaService;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Creación y consulta de facturas")
public class FacturaController {
    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // Backwards compatible Map-based endpoint
    @Operation(summary = "Crear factura (map)", description = "Crea una factura usando un payload genérico",
        responses = {@ApiResponse(responseCode = "201", description = "Factura creada", content = @Content)})
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearFactura(@RequestBody Map<String, Object> body) {
        var r = facturaService.crearFactura(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    // New typed endpoint accepting DTO
    @Operation(summary = "Crear factura (DTO)", description = "Crea una factura usando DTO tipado",
        responses = {@ApiResponse(responseCode = "201", description = "Factura creada", content = @Content(mediaType = "application/json",
            examples = @ExampleObject(value = "{\"factura\": {\"id\": \"f1\", \"total\": 123.45}}")) )})
    @PostMapping(path = "/dto", consumes = "application/json")
    public ResponseEntity<?> crearFacturaDTO(@Valid @RequestBody FacturaRequest facturaRequest) {
        try {
            var resp = facturaService.crearFactura(facturaRequest);
            return ResponseEntity.status(201).body(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(
        summary = "Checkout carrito",
        description = "Crea una factura a partir de un carrito del usuario autenticado. Convierte los items del carrito en una factura y actualiza el stock.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            description = "ID del carrito a facturar",
            content = @Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Checkout",
                    value = "{\"carritoId\":\"507f1f77bcf86cd799439999\"}"
                )
            )
        ),
        responses = {
            @ApiResponse(responseCode = "201", description = "Factura creada exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"factura\":{\"id\":\"507f1f77bcf86cd799439888\",\"numeroFactura\":\"FAC-2024-001\",\"usuarioId\":\"507f1f77bcf86cd799439011\",\"total\":127.50,\"fecha\":\"2024-10-30T10:30:00Z\",\"items\":[{\"productoId\":\"507f191e810c19729de860ea\",\"cantidad\":5,\"precioUnitario\":25.50}]}}"
                    )
                )
            ),
            @ApiResponse(responseCode = "400", description = "Carrito inválido o vacío", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "409", description = "Stock insuficiente", content = @Content)
        }
    )
    @PostMapping(path = "/checkout", consumes = "application/json")
    public ResponseEntity<?> checkout(@RequestBody Map<String, Object> body, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        try {
            String carritoId = (String) body.get("carritoId");
            var resp = facturaService.checkout(carritoId, userId);
            return ResponseEntity.status(201).body(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(400).body(Map.of("error", iae.getMessage()));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(Map.of("error", ise.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }

    }

    @Operation(summary = "Obtener factura por id", responses = {@ApiResponse(responseCode = "200", description = "Factura encontrada", content = @Content),
        @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)})
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactura(@PathVariable String id) {
        var maybe = facturaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", maybe.get()));
    }

    @Operation(summary = "Obtener factura por número", responses = {@ApiResponse(responseCode = "200", description = "Factura encontrada", content = @Content),
        @ApiResponse(responseCode = "404", description = "No encontrada", content = @Content)})
    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getPorNumero(@PathVariable String numero) {
        Factura f = facturaService.findByNumeroFactura(numero);
        if (f == null) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", f));
    }

    @Operation(summary = "Listar facturas por usuario", responses = {@ApiResponse(responseCode = "200", description = "Lista de facturas", content = @Content)})
    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String userId) {
        if (userId == null) return ResponseEntity.ok(Map.of("facturas", List.of()));
        List<Factura> r = facturaService.listarPorUsuario(userId);
        return ResponseEntity.ok(Map.of("facturas", r));
    }

}
