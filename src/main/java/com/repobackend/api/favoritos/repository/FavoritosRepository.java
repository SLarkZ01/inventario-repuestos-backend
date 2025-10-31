package com.repobackend.api.favoritos.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.favoritos.model.Favorite;

public interface FavoriteRepository extends MongoRepository<Favorite, String> {
    List<Favorite> findByUsuarioId(String usuarioId);
    Page<Favorite> findByUsuarioId(String usuarioId, Pageable p);
    Favorite findByUsuarioIdAndProductoId(String usuarioId, String productoId);
    void deleteByUsuarioIdAndProductoId(String usuarioId, String productoId);
}
