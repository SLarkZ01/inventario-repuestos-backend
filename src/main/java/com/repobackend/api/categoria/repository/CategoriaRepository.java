package com.repobackend.api.categoria.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.categoria.model.Categoria;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {
    Categoria findByIdString(String idString);
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
    Page<Categoria> findByNombreContainingIgnoreCase(String nombre, Pageable p);
}
