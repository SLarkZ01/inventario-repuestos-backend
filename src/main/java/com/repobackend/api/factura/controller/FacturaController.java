package com.repobackend.api.factura.controller;

import java.util.List;
import java.util.Map;

import com.repobackend.api.factura.dto.FacturaRequest;
import com.repobackend.api.factura.service.FacturaServiceV2;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.repobackend.api.factura.model.Factura;
import com.repobackend.api.factura.service.FacturaPdfService;

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
@Tag(name = "Facturas", description = "Creación y consulta de facturas con IVA y descuento de stock")
public class FacturaController {
    private final FacturaServiceV2 facturaService;
    private final FacturaPdfService facturaPdfService;

    public FacturaController(FacturaServiceV2 facturaService, FacturaPdfService facturaPdfService) {
        this.facturaService = facturaService;
        this.facturaPdfService = facturaPdfService;
    }

    @Operation(
        summary = "Crear factura EMITIDA",
        description = "Crea y emite una factura definitiva. SIEMPRE descuenta stock y calcula precios/IVA desde productos. No acepta precios del cliente.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Factura directa",
                    value = "{\n  \"items\": [ { \"productoId\": \"507f1f77bcf86cd799439011\", \"cantidad\": 2 } ],\n  \"cliente\": { \"nombre\": \"Juan Pérez\", \"documento\": \"123456789\", \"direccion\": \"Calle 123\" }\n}"
                )
            )
        )
    )
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearFactura(
        @Valid @RequestBody FacturaRequest facturaRequest,
        Authentication authentication
    ) {
        try {
            String userId = authentication == null ? null : authentication.getName();
            var resp = facturaService.crearYEmitir(facturaRequest, userId);
            return ResponseEntity.status(201).body(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(Map.of("error", ise.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(
        summary = "Crear factura en BORRADOR",
        description = "Crea factura sin descontar stock (para cotizaciones). Usar POST /facturas/{id}/emitir para emitirla.",
        requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            content = @io.swagger.v3.oas.annotations.media.Content(
                mediaType = "application/json",
                examples = @ExampleObject(
                    name = "Borrador",
                    value = "{\n  \"items\": [ { \"productoId\": \"507f1f77bcf86cd799439011\", \"cantidad\": 1 } ],\n  \"cliente\": { \"nombre\": \"Empresa XYZ\" }\n}"
                )
            )
        )
    )
    @PostMapping(path = "/borrador", consumes = "application/json")
    public ResponseEntity<?> crearBorrador(
        @Valid @RequestBody FacturaRequest facturaRequest,
        Authentication authentication
    ) {
        try {
            String userId = authentication == null ? null : authentication.getName();
            var resp = facturaService.crearBorrador(facturaRequest, userId);
            return ResponseEntity.status(201).body(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(
        summary = "Emitir borrador",
        description = "Emite un borrador (descuenta stock y cambia estado a EMITIDA)"
    )
    @PostMapping("/{id}/emitir")
    public ResponseEntity<?> emitirBorrador(@PathVariable String id, Authentication authentication) {
        try {
            String userId = authentication == null ? null : authentication.getName();
            var resp = facturaService.emitirBorrador(id, userId);
            return ResponseEntity.ok(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(404).body(Map.of("error", iae.getMessage()));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(Map.of("error", ise.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(
        summary = "Anular factura",
        description = "Anula una factura emitida (NO devuelve stock automáticamente)"
    )
    @PostMapping("/{id}/anular")
    public ResponseEntity<?> anularFactura(
        @PathVariable String id,
        @RequestBody(required = false) Map<String, String> body
    ) {
        try {
            String motivo = body != null ? body.get("motivo") : "Sin motivo especificado";
            var resp = facturaService.anular(id, motivo);
            return ResponseEntity.ok(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(404).body(Map.of("error", iae.getMessage()));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(Map.of("error", ise.getMessage()));
        } catch (Exception ex) {
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(
        summary = "Checkout carrito",
        description = "Crea factura EMITIDA desde carrito. SIEMPRE descuenta stock y calcula precios/IVA desde productos."
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

    @Operation(summary = "Obtener factura por id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactura(@PathVariable String id) {
        var maybe = facturaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", maybe.get()));
    }

    @Operation(summary = "Descargar PDF de factura con IVA")
    @GetMapping(value = "/{id}/pdf", produces = MediaType.APPLICATION_PDF_VALUE)
    public ResponseEntity<byte[]> descargarPdf(@PathVariable String id) {
        var maybe = facturaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).build();
        Factura f = maybe.get();
        byte[] pdf = facturaPdfService.renderFacturaPdf(f);
        String filename = "factura-" + (f.getNumeroFactura() == null ? f.getId() : f.getNumeroFactura()) + ".pdf";
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("inline", filename);
        return ResponseEntity.ok().headers(headers).body(pdf);
    }

    @Operation(summary = "Obtener factura por número")
    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getPorNumero(@PathVariable String numero) {
        Factura f = facturaService.findByNumeroFactura(numero);
        if (f == null) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", f));
    }

    @Operation(summary = "Listar facturas por usuario")
    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String userId) {
        if (userId == null) return ResponseEntity.ok(Map.of("facturas", List.of()));
        List<Factura> r = facturaService.listarPorUsuario(userId);
        return ResponseEntity.ok(Map.of("facturas", r));
    }

}
