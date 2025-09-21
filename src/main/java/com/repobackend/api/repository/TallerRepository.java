package com.repobackend.api.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.model.Taller;

public interface TallerRepository extends MongoRepository<Taller, String> {
    List<Taller> findByOwnerId(String ownerId);
}
