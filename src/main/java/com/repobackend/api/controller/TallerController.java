package com.repobackend.api.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.service.TallerService;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/talleres")
public class TallerController {
    private final TallerService tallerService;

    public TallerController(TallerService tallerService) {
        this.tallerService = tallerService;
    }

    // Create a taller (owner is the authenticated user)
    @PostMapping
    public ResponseEntity<?> crearTaller(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String nombre = body.get("nombre");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearTaller(userId, nombre);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @PostMapping("/{tallerId}/almacenes")
    public ResponseEntity<?> crearAlmacen(@PathVariable String tallerId, @RequestBody Map<String, String> body, HttpServletRequest req) {
        String nombre = body.get("nombre");
        String ubicacion = body.get("ubicacion");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearAlmacen(userId, tallerId, nombre, ubicacion);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @PostMapping("/{tallerId}/invitaciones/codigo")
    public ResponseEntity<?> crearInvitacionCodigo(@PathVariable String tallerId, @RequestBody Map<String, Object> body, HttpServletRequest req) {
        String role = (String) body.getOrDefault("role", "VENDEDOR");
        int days = body.get("daysValid") == null ? 7 : ((Number) body.get("daysValid")).intValue();
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.crearInvitacionCodigo(userId, tallerId, role, days);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.status(201).body(r);
    }

    @PostMapping("/invitaciones/accept")
    public ResponseEntity<?> acceptInvitacion(@RequestBody Map<String, String> body, HttpServletRequest req) {
        String code = body.get("code");
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        Map<String, Object> r = tallerService.acceptInvitationByCode(userId, code);
        if (r.containsKey("error")) return ResponseEntity.badRequest().body(r);
        return ResponseEntity.ok(r);
    }

    // Optional: list talleres of authenticated user
    @GetMapping
    public ResponseEntity<?> listMyTalleres(HttpServletRequest req) {
        String userId = req.getUserPrincipal() == null ? null : req.getUserPrincipal().getName();
        if (userId == null) return ResponseEntity.status(401).body(Map.of("error", "No autenticado"));
        return ResponseEntity.ok(Map.of("talleres", tallerService.getTalleresByOwner(userId)));
    }
}
