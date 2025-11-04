package com.repobackend.api.producto.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.producto.model.Producto;

public interface ProductoRepository extends MongoRepository<Producto, String> {
    Producto findByIdString(String idString);
    List<Producto> findByCategoriaId(String categoriaId);
    List<Producto> findByNombreContainingIgnoreCase(String nombre);
    Page<Producto> findByNombreContainingIgnoreCase(String nombre, Pageable p);
    Page<Producto> findAll(Pageable p);
    Page<Producto> findByCategoriaId(String categoriaId, Pageable p);
    Page<Producto> findByTallerId(String tallerId, Pageable p);
    List<Producto> findByTallerId(String tallerId);
}
