package com.repobackend.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.model.Factura;
import com.repobackend.api.service.FacturaService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/facturas")
public class FacturaController {
    private final FacturaService facturaService;

    public FacturaController(FacturaService facturaService) {
        this.facturaService = facturaService;
    }

    // Backwards compatible Map-based endpoint
    @PostMapping(consumes = "application/json")
    public ResponseEntity<?> crearFactura(@RequestBody Map<String, Object> body, HttpServletRequest req) {
        var r = facturaService.crearFactura(body);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    // New typed endpoint accepting DTO
    @PostMapping(path = "/dto", consumes = "application/json")
    public ResponseEntity<?> crearFacturaDTO(@Valid @RequestBody com.repobackend.api.dto.FacturaRequest facturaRequest) {
        try {
            var resp = facturaService.crearFactura(facturaRequest);
            return ResponseEntity.status(201).body(Map.of("factura", resp));
        } catch (IllegalArgumentException iae) {
            return ResponseEntity.badRequest().body(Map.of("error", iae.getMessage()));
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
