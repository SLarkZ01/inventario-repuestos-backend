package com.repobackend.api.favoritos.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.favoritos.model.Favoritos;

public interface FavoritosRepository extends MongoRepository<Favoritos, String> {
    List<Favoritos> findByUsuarioId(String usuarioId);
    Page<Favoritos> findByUsuarioId(String usuarioId, Pageable p);
    Favoritos findByUsuarioIdAndProductoId(String usuarioId, String productoId);
    void deleteByUsuarioIdAndProductoId(String usuarioId, String productoId);
}
