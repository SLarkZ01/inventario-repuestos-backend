package com.repobackend.api.favoritos.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.repobackend.api.favoritos.service.FavoritosService;

// OpenAPI annotations
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;

@RestController
@RequestMapping("/api/favoritos")
@Tag(name = "Favoritos", description = "Operaciones para gestionar productos favoritos del usuario")
public class FavoritosController {
    private final FavoritosService favoritosService;

    public FavoritosController(FavoritosService favoritosService) {
        this.favoritosService = favoritosService;
    }

    @Operation(
        summary = "Agregar producto a favoritos",
        description = "Añade un producto a la lista de favoritos del usuario autenticado",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "productoId", description = "ID del producto a agregar", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "201", description = "Favorito creado exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"favorito\":{\"id\":\"507fabc123def456789012ab\",\"productoId\":\"507f191e810c19729de860ea\",\"usuarioId\":\"507f1f77bcf86cd799439011\",\"fechaCreacion\":\"2024-10-30T10:30:00Z\"}}")
                )
            ),
            @ApiResponse(responseCode = "400", description = "Solicitud inválida", content = @Content),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content),
            @ApiResponse(responseCode = "404", description = "Producto no encontrado", content = @Content)
        }
    )
    @PostMapping("/productos/{productoId}")
    public ResponseEntity<?> add(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = favoritosService.addFavorite(userId, productoId);
        if (r.containsKey("error")) {
            Object err = r.get("error");
            if (err != null && err.toString().contains("Producto no encontrado")) {
                return ResponseEntity.status(404).body(Map.of("error", err));
            }
            return ResponseEntity.badRequest().body(r);
        }
        Object fav = r.get("favorite");
        return ResponseEntity.status(201).body(Map.of("favorito", fav));
    }

    @Operation(
        summary = "Remover producto de favoritos",
        description = "Remueve un producto de la lista de favoritos del usuario autenticado",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "productoId", description = "ID del producto a remover", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Favorito removido exitosamente",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"exito\":true,\"mensaje\":\"Producto removido de favoritos\"}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @DeleteMapping("/productos/{productoId}")
    public ResponseEntity<?> remove(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = favoritosService.removeFavorite(userId, productoId);
        boolean deleted = Boolean.TRUE.equals(r.get("deleted"));
        return ResponseEntity.ok(Map.of(
            "exito", deleted,
            "mensaje", deleted ? "Producto removido de favoritos" : "El producto no estaba en favoritos"
        ));
    }

    @Operation(
        summary = "Listar favoritos",
        description = "Devuelve una lista paginada de productos favoritos del usuario autenticado",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "page", description = "Número de página (base 0)", example = "0"),
            @io.swagger.v3.oas.annotations.Parameter(name = "size", description = "Elementos por página", example = "20")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Lista de favoritos",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(
                        value = "{\"favoritos\":[{\"id\":\"507fabc123def456789012ab\",\"productoId\":\"507f191e810c19729de860ea\",\"producto\":{\"nombre\":\"Filtro de Aceite\",\"precio\":25.50}}],\"page\":0,\"size\":20,\"total\":5}"
                    )
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false, defaultValue = "0") int page,
                                  @RequestParam(required = false, defaultValue = "20") int size,
                                  Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = favoritosService.listFavorites(userId, page, size);
        var items = r.get("favorites");
        var total = r.get("total");
        return ResponseEntity.ok(Map.of(
            "favoritos", items,
            "total", total,
            "page", page,
            "size", size
        ));
    }

    @Operation(
        summary = "Comprobar favorito",
        description = "Comprueba si un producto está en la lista de favoritos del usuario autenticado",
        parameters = {
            @io.swagger.v3.oas.annotations.Parameter(name = "productoId", description = "ID del producto a verificar", required = true, example = "507f191e810c19729de860ea")
        },
        responses = {
            @ApiResponse(responseCode = "200", description = "Estado del favorito",
                content = @Content(mediaType = "application/json",
                    examples = @ExampleObject(value = "{\"favorito\":true}")
                )
            ),
            @ApiResponse(responseCode = "401", description = "Usuario no autenticado", content = @Content)
        }
    )
    @GetMapping("/productos/{productoId}/es-favorito")
    public ResponseEntity<?> isFav(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        boolean fav = favoritosService.isFavorite(userId, productoId);
        return ResponseEntity.ok(Map.of("favorito", fav));
    }
}
