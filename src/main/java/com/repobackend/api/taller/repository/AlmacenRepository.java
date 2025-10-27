package com.repobackend.api.taller.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.taller.model.Almacen;

public interface AlmacenRepository extends MongoRepository<Almacen, String> {
    List<Almacen> findByTallerId(String tallerId);
    long countByTallerId(String tallerId);
}
