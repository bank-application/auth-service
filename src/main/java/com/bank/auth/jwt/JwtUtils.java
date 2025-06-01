package com.bank.auth.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.List;

@Component
public class JwtUtils {

    private static final long EXPIRATION_TIME_MS = 1000 * 60 * 60; // 1 hour validity
    private final Key secretKey = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public String generateToken(String email, String mobile, List<String> roles) {
        JwtBuilder builder = Jwts.builder()
                .setSubject(email != null ? email : mobile)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME_MS))
                .signWith(secretKey);
        if (email != null && !email.isBlank()) {
            builder.claim("email", email);
        }
        if (mobile != null && !mobile.isBlank()) {
            builder.claim("mobile", mobile);
        }
        builder.claim("roles", roles);
        return builder.compact();
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(secretKey)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).get("email", String.class);
    }

    public String extractMobile(String token) {
        return extractAllClaims(token).get("mobile", String.class);
    }

    public List<String> extractRoles(String token) {
        return extractAllClaims(token).get("roles", List.class);
    }

    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
