package com.repobackend.api.controller;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.dto.MovimientoRequest;
import com.repobackend.api.dto.MovimientoResponse;
import com.repobackend.api.model.Movimiento;
import com.repobackend.api.service.MovimientoService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/movimientos")
public class MovimientoController {
    private final MovimientoService movimientoService;

    public MovimientoController(MovimientoService movimientoService) {
        this.movimientoService = movimientoService;
    }

    @PostMapping
    public ResponseEntity<?> crearMovimiento(@Valid @RequestBody MovimientoRequest body, HttpServletRequest req) {
        MovimientoResponse resp = movimientoService.crearMovimiento(body);
        return ResponseEntity.status(201).body(Map.of("movimiento", resp));
    }

    @GetMapping
    public ResponseEntity<?> listar(@RequestParam(required = false) String productoId, @RequestParam(required = false) String tipo) {
        if (productoId != null) {
            List<Movimiento> res = movimientoService.listarPorProducto(productoId);
            return ResponseEntity.ok(Map.of("movimientos", res));
        }
        if (tipo != null) {
            List<Movimiento> res = movimientoService.listarPorTipo(tipo);
            return ResponseEntity.ok(Map.of("movimientos", res));
        }
        List<Movimiento> all = movimientoService.listarTodos();
        return ResponseEntity.ok(Map.of("movimientos", all));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getMovimiento(@PathVariable String id) {
        var maybe = movimientoService.getById(id);
        if (maybe.isEmpty()) return ResponseEntity.status(404).body(Map.of("error", "Movimiento no encontrado"));
        return ResponseEntity.ok(Map.of("movimiento", maybe.get()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateMovimiento(@PathVariable String id, @Valid @RequestBody MovimientoRequest body) {
        // Para mantener integridad y trazabilidad de movimientos consideramos
        // los movimientos como registros históricos inmutables. En lugar de
        // permitir la edición directa (PUT), se recomienda crear un movimiento
        // compensatorio (ej. un egreso para revertir un ingreso) usando POST /api/movimientos.
        //
        // Esto evita problemas de consistencia en el stock y facilita auditoría.
        return ResponseEntity.status(405).body(Map.of(
            "error", "PUT no soportado para movimientos. Use POST para crear un movimiento compensatorio o contacte al administrador.",
            "hint", "Para corregir un movimiento cree un nuevo movimiento de tipo contrario (egreso/ingreso) con la cantidad correcta."
        ));
    }
}
