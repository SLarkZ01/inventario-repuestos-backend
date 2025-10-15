package com.repobackend.api.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.model.Categoria;

public interface CategoriaRepository extends MongoRepository<Categoria, String> {
    Categoria findByIdString(String idString);
    List<Categoria> findByNombreContainingIgnoreCase(String nombre);
}
