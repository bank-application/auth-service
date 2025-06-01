package com.bank.auth.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenCreateDto {
    private String token;
    private String issuedAt;
    private String expiresAt;
    private boolean revoked;
}
