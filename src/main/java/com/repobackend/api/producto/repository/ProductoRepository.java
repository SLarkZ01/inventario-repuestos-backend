package com.repobackend.api.producto.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.producto.model.Producto;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    Producto findByIdString(String idString);
    List<Producto> findByCategoriaId(String categoriaId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
}
