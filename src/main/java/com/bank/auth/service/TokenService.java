package com.bank.auth.service;

import com.bank.auth.dto.TokenCreateDto;
import com.bank.auth.entity.Token;

public interface TokenService {
    Token createToken(TokenCreateDto tokenCreateDto);
}
