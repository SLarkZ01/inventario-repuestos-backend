package com.repobackend.api.stock.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.stock.service.StockService;

@RestController
@RequestMapping("/api/stock")
public class StockController {
    private final StockService stockService;

    public StockController(StockService stockService) {
        this.stockService = stockService;
    }

    @GetMapping
    public ResponseEntity<?> getByProducto(@RequestParam String productoId) {
        var rows = stockService.getStockByProducto(productoId);
        return ResponseEntity.ok(Map.of("stockByAlmacen", rows, "total", stockService.getTotalStock(productoId)));
    }

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

    @DeleteMapping
    public ResponseEntity<?> delete(@RequestParam String productoId, @RequestParam String almacenId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = stockService.removeStockRecord(productoId, almacenId, userId);
        if (r.containsKey("error")) return ResponseEntity.status(404).body(r);
        return ResponseEntity.ok(r);
    }
}
