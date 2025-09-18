package com.repobackend.api.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.repobackend.api.model.RefreshToken;

public interface RefreshTokenRepository extends MongoRepository<RefreshToken, String> {
    Optional<RefreshToken> findByTokenHash(String tokenHash);
}
