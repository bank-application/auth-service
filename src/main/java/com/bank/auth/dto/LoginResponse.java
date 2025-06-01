package com.bank.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LoginResponse {
    private String token;
    private String tokenType;
    private String issuedAt;
    private String expiresAt;
}
