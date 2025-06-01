package com.bank.auth.service;

import com.bank.auth.dto.LoginRequestDto;
import com.bank.auth.dto.LoginResponse;
import com.bank.auth.dto.RegisterRequestDto;
import com.bank.auth.dto.RegisterResponseDto;

public interface AuthService {
    RegisterResponseDto registerNewUser(RegisterRequestDto registerRequestDto);

    LoginResponse login(LoginRequestDto loginRequestDto);
}
