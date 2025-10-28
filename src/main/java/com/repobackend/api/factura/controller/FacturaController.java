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

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {
    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // Backwards compatible Map-based endpoint
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearFactura(@RequestBody Map<String, Object> body) {
        var r = facturaService.crearFactura(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    // New typed endpoint accepting DTO
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

    @GetMapping("/{id}")
    public ResponseEntity<?> getFactura(@PathVariable String id) {
        var maybe = facturaService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", maybe.get()));
    }

    @GetMapping("/numero/{numero}")
    public ResponseEntity<?> getPorNumero(@PathVariable String numero) {
        Factura f = facturaService.findByNumeroFactura(numero);
        if (f == null) return ResponseEntity.status(404).body(Map.of("error", "Factura no encontrada"));
        return ResponseEntity.ok(Map.of("factura", f));
    }

    @GetMapping
    public ResponseEntity<?> listarPorUsuario(@RequestParam(required = false) String userId) {
        if (userId == null) return ResponseEntity.ok(Map.of("facturas", List.of()));
        List<Factura> r = facturaService.listarPorUsuario(userId);
        return ResponseEntity.ok(Map.of("facturas", r));
    }

}
