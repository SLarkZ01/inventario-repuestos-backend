package com.repobackend.api.taller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.taller.service.TallerService;

@RestController
@RequestMapping("/api/talleres/{tallerId}/miembros")
public class TallerMemberController {
    private final TallerService tallerService;

    public TallerMemberController(TallerService tallerService) {
        this.tallerService = tallerService;
    }

    /**
     * Promover a un miembro a ADMIN dentro del taller.
     * Solo puede hacerlo el owner o un miembro ADMIN.
     */
    @PostMapping("/{memberUserId}/promote")
    public ResponseEntity<?> promote(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.promoteMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Demover a un miembro (remover rol ADMIN) dentro del taller.
     */
    @PostMapping("/{memberUserId}/demote")
    public ResponseEntity<?> demote(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.demoteMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }

    /**
     * Remover un miembro del taller.
     */
    @DeleteMapping("/{memberUserId}")
    public ResponseEntity<?> remove(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.removeMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }
}

