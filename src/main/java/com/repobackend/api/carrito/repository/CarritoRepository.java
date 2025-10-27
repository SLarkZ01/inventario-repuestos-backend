package com.repobackend.api.carrito.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.carrito.model.Carrito;

public interface CarritoRepository extends MongoRepository<Carrito, String> {
    List<Carrito> findByUsuarioId(String usuarioId);
}
