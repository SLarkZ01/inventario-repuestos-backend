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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.ArraySchema;

@RestController
@RequestMapping("/api/facturas")
@Tag(name = "Facturas", description = "Creación y consulta de facturas con IVA y descuento de stock")
public class FacturaController {
    private static final Logger logger = LoggerFactory.getLogger(FacturaController.class);
    private final FacturaServiceV2 facturaService;
    private final FacturaPdfService facturaPdfService;

    public FacturaController(FacturaServiceV2 facturaService, FacturaPdfService facturaPdfService) {
        this.facturaService = facturaService;
        this.facturaPdfService = facturaPdfService;
    }

    @Operation(
        summary = "Crear factura EMITIDA",
        description = "Crea y emite una factura definitiva. SIEMPRE descuenta stock y calcula precios/IVA desde productos. No acepta precios del cliente."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Factura creada y emitida",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflicto (p.ej. stock insuficiente)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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
        description = "Crea factura sin descontar stock (para cotizaciones). Usar POST /facturas/{id}/emitir para emitirla."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Borrador creado",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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

    @Operation(summary = "Emitir borrador",
        description = "Emite un borrador (descuenta stock y cambia estado a EMITIDA)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Borrador emitido",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "404", description = "Borrador no encontrado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflicto al emitir", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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

    @Operation(summary = "Anular factura",
        description = "Anula una factura emitida (NO devuelve stock automáticamente)"
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura anulada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content),
        @ApiResponse(responseCode = "409", description = "No se puede anular", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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

    @Operation(summary = "Eliminar factura borrador",
        description = "Elimina una factura en estado BORRADOR (cotización rechazada o descartada). Solo se pueden eliminar facturas que NO han sido emitidas."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Borrador eliminado exitosamente",
            content = @Content(mediaType = "application/json") ),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content),
        @ApiResponse(responseCode = "409", description = "No se puede eliminar (solo BORRADOR)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarBorrador(@PathVariable String id) {
        try {
            facturaService.eliminarBorrador(id);
            return ResponseEntity.ok(Map.of("deleted", true, "message", "Factura borrador eliminada exitosamente"));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.status(404).body(Map.of("error", iae.getMessage()));
        } catch (IllegalStateException ise) {
            return ResponseEntity.status(409).body(Map.of("error", ise.getMessage()));
        } catch (Exception ex) {
            logger.error("Error eliminando borrador: {}", ex.getMessage(), ex);
            return ResponseEntity.status(500).body(Map.of("error", ex.getMessage()));
        }
    }

    @Operation(summary = "Checkout carrito",
        description = "Crea factura EMITIDA desde carrito. SIEMPRE descuenta stock y calcula precios/IVA desde productos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Factura creada desde carrito",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "400", description = "Datos inválidos", content = @Content),
        @ApiResponse(responseCode = "401", description = "No autenticado", content = @Content),
        @ApiResponse(responseCode = "409", description = "Conflicto (stock)", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content)
    })
    @GetMapping("/{id}")
    public ResponseEntity<?> getFactura(@PathVariable String id) {
        var maybe = facturaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", maybe.get()));
    }

    @Operation(summary = "Descargar PDF de factura con IVA")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "PDF de la factura",
            content = @Content(mediaType = "application/pdf", schema = @Schema(type = "string", format = "binary")) ),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content),
        @ApiResponse(responseCode = "500", description = "Error interno", content = @Content)
    })
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
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Factura encontrada",
            content = @Content(mediaType = "application/json", schema = @Schema(implementation = Factura.class)) ),
        @ApiResponse(responseCode = "404", description = "Factura no encontrada", content = @Content)
    })
    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getPorNumero(@PathVariable String numero) {
        Factura f = facturaService.findByNumeroFactura(numero);
        if (f == null) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", f));
    }

    @Operation(summary = "Listar facturas",
        description = "Lista facturas. Usuarios ADMIN ven todas las facturas. Usuarios normales ven solo las facturas donde son cliente o las crearon.")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Lista de facturas",
            content = @Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = Factura.class))) )
    })
    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String userId, Authentication authentication) {
        // Verificar si es usuario ADMIN
        boolean isAdmin = authentication != null &&
                         authentication.getAuthorities().stream()
                             .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN") || auth.getAuthority().equals("ADMIN"));

        logger.debug("Listar facturas - isAdmin={}, userId param='{}', authName='{}'",
                    isAdmin, userId, authentication == null ? null : authentication.getName());

        // Si es ADMIN, devolver TODAS las facturas
        if (isAdmin) {
            List<Factura> all = facturaService.listarTodas();
            logger.debug("Usuario ADMIN - devolviendo {} facturas totales", all.size());
            return ResponseEntity.ok(Map.of("facturas", all));
        }

        // Usuario normal: filtrar por userId (cliente o creador)
        if (userId == null) {
            userId = authentication == null ? null : authentication.getName();
        }

        if (userId == null) {
            logger.debug("No userId disponible en request; devolviendo lista vacía");
            return ResponseEntity.ok(Map.of("facturas", List.of()));
        }

        List<Factura> r = facturaService.listarPorUsuario(userId);
        logger.debug("Facturas encontradas para userId {}: {}", userId, r == null ? 0 : r.size());
        return ResponseEntity.ok(Map.of("facturas", r));
    }

}
