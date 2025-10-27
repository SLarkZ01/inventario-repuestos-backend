package com.repobackend.api.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.model.Producto;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    Producto findByIdString(String idString);
    List<Producto> findByCategoriaId(String categoriaId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
