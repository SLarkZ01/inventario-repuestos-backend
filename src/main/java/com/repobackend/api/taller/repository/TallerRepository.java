package com.repobackend.api.taller.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.repobackend.api.taller.model.Taller;

public interface TallerRepository extends MongoRepository<Taller, String> {
    List<Taller> findByOwnerId(String ownerId);
    // Busca talleres donde en el array miembros exista un objeto con userId igual
    @Query("{ 'miembros.userId': ?0 }")
    List<Taller> findByMiembrosUserId(String userId);
}
