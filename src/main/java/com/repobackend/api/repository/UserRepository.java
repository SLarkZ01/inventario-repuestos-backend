package com.repobackend.api.repository;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import com.repobackend.api.model.User;

public interface UserRepository extends MongoRepository<User, String> {
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByUsername(String username);
}
