package com.repobackend.api.auth.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.repobackend.api.auth.model.User;

public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    Optional<User> findByProviderAndProviderId(String provider, String providerId);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    @Query(value = "{ 'roles' : ?0 }", count = true)
    long countByRole(String role);

}
