package com.bank.auth.repository;

import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Token;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TokenRepo extends MongoRepository<Token, String> {
    Optional<Token> findBySubject(String subject);
}
