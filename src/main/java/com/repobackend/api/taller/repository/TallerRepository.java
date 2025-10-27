package com.repobackend.api.taller.repository;

import java.util.List;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.taller.model.Taller;

public interface TallerRepository extends MongoRepository<Taller, String> {
    List<Taller> findByOwnerId(String ownerId);
}
