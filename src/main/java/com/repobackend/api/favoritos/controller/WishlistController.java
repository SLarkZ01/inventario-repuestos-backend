package com.repobackend.api.wishlist.controller;

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

import com.repobackend.api.wishlist.service.WishlistService;

@RestController
@RequestMapping("/api/wishlist")
public class WishlistController {
    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @PostMapping("/products/{productoId}")
    public ResponseEntity<?> add(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = wishlistService.addFavorite(userId, productoId);
        if (r.containsKey("error")) {
            Object err = r.get("error");
            if (err != null && err.toString().contains("Producto no encontrado")) {
                return ResponseEntity.status(404).body(r);
            }
            return ResponseEntity.badRequest().body(r);
        }
        return ResponseEntity.status(201).body(r);
    }

    @DeleteMapping("/products/{productoId}")
    public ResponseEntity<?> remove(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = wishlistService.removeFavorite(userId, productoId);
        return ResponseEntity.ok(r);
    }

    @GetMapping
    public ResponseEntity<?> list(@RequestParam(required = false, defaultValue = "0") int page,
                                  @RequestParam(required = false, defaultValue = "20") int size,
                                  Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        var r = wishlistService.listFavorites(userId, page, size);
        return ResponseEntity.ok(r);
    }

    @GetMapping("/products/{productoId}/is")
    public ResponseEntity<?> isFav(@PathVariable String productoId, Authentication authentication) {
        String userId = authentication == null ? null : authentication.getName();
        boolean fav = wishlistService.isFavorite(userId, productoId);
        return ResponseEntity.ok(Map.of("favorite", fav));
    }
}
