package com.repobackend.api.taller.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.repobackend.api.taller.model.Invitation;

public interface InvitationRepository extends MongoRepository<Invitation, String> {
    Optional<Invitation> findByCodeHash(String codeHash);
}
