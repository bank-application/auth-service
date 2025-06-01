package com.bank.auth.repository;

import com.bank.auth.entity.AuthUser;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthRepo extends MongoRepository<AuthUser, String> {
    Optional<AuthUser> findByEmail(String email);
}
