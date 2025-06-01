package com.bank.auth.service.impl;

import com.bank.auth.dto.TokenCreateDto;
import com.bank.auth.entity.Token;
import com.bank.auth.repository.TokenRepo;
import com.bank.auth.service.TokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TokenServiceImpl implements TokenService {

    @Autowired
    private TokenRepo tokenRepo;

    @Override
    public Token createToken(TokenCreateDto tokenCreateDto) {
        Token token = Token.builder()
                .id(UUID.randomUUID().toString())
                .token(tokenCreateDto.getToken())
                .issuedAt(tokenCreateDto.getIssuedAt())
                .expiresAt(tokenCreateDto.getExpiresAt())
                .revoked(tokenCreateDto.isRevoked())
                .build();
        return tokenRepo.save(token);
    }
}
