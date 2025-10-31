package com.repobackend.api.taller.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.taller.service.TallerService;

// OpenAPI
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/talleres/{tallerId}/miembros")
@Tag(name = "TallerMiembros", description = "Operaciones sobre miembros de taller (promover, demover, remover)")
public class TallerMemberController {
    private final TallerService tallerService;

    public TallerMemberController(TallerService tallerService) {
        this.tallerService = tallerService;
    }

    @Operation(
        summary = "Promover miembro a ADMIN",
        description = "Otorga permisos de administrador a un miembro del taller. Solo puede hacerlo el owner o un miembro ADMIN.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller", required = true, example = "507f1f77bcf86cd799439777"),
            @io.swagger.v3.oas.annotations.Parameter(name = "memberUserId", description = "ID del usuario a promover", required = true, example = "507f1f77bcf86cd799439011")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Miembro promovido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"userId\":\"507f1f77bcf86cd799439011\",\"newRole\":\"ADMIN\",\"message\":\"Usuario promovido a ADMIN\"}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para esta operación", content = @Content),
            @ApiResponse(responseCode = "404", description = "Taller o miembro no encontrado", content = @Content)
        }
    )
    @PostMapping("/{memberUserId}/promote")
    public ResponseEntity<?> promote(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.promoteMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Demover miembro (remover rol ADMIN)",
        description = "Remueve los permisos de administrador a un miembro del taller. Solo puede hacerlo el owner.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller", required = true, example = "507f1f77bcf86cd799439777"),
            @io.swagger.v3.oas.annotations.Parameter(name = "memberUserId", description = "ID del usuario a demover", required = true, example = "507f1f77bcf86cd799439011")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Miembro demovido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"userId\":\"507f1f77bcf86cd799439011\",\"newRole\":\"VENDEDOR\",\"message\":\"Permisos de ADMIN removidos\"}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para esta operación", content = @Content),
            @ApiResponse(responseCode = "404", description = "Taller o miembro no encontrado", content = @Content)
        }
    )
    @PostMapping("/{memberUserId}/demote")
    public ResponseEntity<?> demote(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.demoteMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }

    @Operation(
        summary = "Remover miembro del taller",
        description = "Elimina un miembro del taller. Solo puede hacerlo el owner o un ADMIN.",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "tallerId", description = "ID del taller", required = true, example = "507f1f77bcf86cd799439777"),
            @io.swagger.v3.oas.annotations.Parameter(name = "memberUserId", description = "ID del usuario a remover", required = true, example = "507f1f77bcf86cd799439011")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Miembro removido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"success\":true,\"userId\":\"507f1f77bcf86cd799439011\",\"message\":\"Miembro removido del taller\"}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "403", description = "No tienes permisos para esta operación", content = @Content),
            @ApiResponse(responseCode = "404", description = "Taller o miembro no encontrado", content = @Content)
        }
    )
    @DeleteMapping("/{memberUserId}")
    public ResponseEntity<?> remove(@PathVariable String tallerId, @PathVariable String memberUserId, Authentication authentication) {
        String callerId = authentication == null ? null : authentication.getName();
        var resp = tallerService.removeMember(callerId, tallerId, memberUserId);
        return ResponseEntity.ok(resp);
    }
}
