package com.repobackend.api.favoritos.service;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.repobackend.api.producto.dto.ProductoResponse;
import com.repobackend.api.producto.service.ProductoService;
import com.repobackend.api.favoritos.model.Favoritos;
import com.repobackend.api.favoritos.repository.FavoritosRepository;

@Service
public class FavoritosService {
    private final FavoritosRepository favoriteRepository;
    private final ProductoService productoService;

    public FavoritosService(FavoritosRepository favoriteRepository, ProductoService productoService) {
        this.favoriteRepository = favoriteRepository;
        this.productoService = productoService;
    }

    public Map<String, Object> addFavorite(String usuarioId, String productoId) {
        if (usuarioId == null || productoId == null) return Map.of("error", "usuarioId y productoId son requeridos");
        // Validar que el producto exista antes de crear el favorito
        try {
            var maybeProd = productoService.getById(productoId);
            if (maybeProd.isEmpty()) {
                return Map.of("error", "Producto no encontrado");
            }
        } catch (Exception ex) {
            // Si ocurre un error al consultar el producto, responder con error general
            return Map.of("error", "Error verificando producto: " + ex.getMessage());
        }
        Favoritos existing = favoriteRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);
        if (existing != null) return Map.of("favorite", existing);
        Favoritos f = new Favoritos();
        f.setUsuarioId(usuarioId);
        f.setProductoId(productoId);
        Favoritos saved = favoriteRepository.save(f);
        return Map.of("favorite", saved);
    }

    public Map<String, Object> removeFavorite(String usuarioId, String productoId) {
        Favoritos existing = favoriteRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);
        if (existing == null) return Map.of("deleted", false);
        favoriteRepository.deleteByUsuarioIdAndProductoId(usuarioId, productoId);
        return Map.of("deleted", true);
    }

    public Map<String, Object> listFavorites(String usuarioId, int page, int size) {
        if (usuarioId == null) return Map.of("favorites", List.of());
        var p = favoriteRepository.findByUsuarioId(usuarioId, PageRequest.of(page, size));

        // Batch fetch: obtener todos los productos de la página en una sola consulta para evitar N+1 queries
        List<String> ids = p.getContent().stream()
            .map(Favoritos::getProductoId)
            .filter(Objects::nonNull)
            .distinct()
            .collect(Collectors.toList());

        Map<String, ProductoResponse> productsMap = productoService.findResponsesByIds(ids);

        var items = p.getContent().stream().map(f -> {
            ProductoResponse pr = productsMap.get(f.getProductoId());
            return Map.of("id", f.getId(), "productoId", f.getProductoId(), "producto", pr, "creadoEn", f.getCreadoEn());
        }).collect(Collectors.toList());

        return Map.of("favorites", items, "total", p.getTotalElements(), "page", page, "size", size);
    }

    public boolean isFavorite(String usuarioId, String productoId) {
        Favoritos f = favoriteRepository.findByUsuarioIdAndProductoId(usuarioId, productoId);
        return f != null;
    }
}
