package com.bank.auth.service.impl;

import com.bank.auth.config.EnvironmentParamConfig;
import com.bank.auth.dto.LoginRequestDto;
import com.bank.auth.dto.LoginResponse;
import com.bank.auth.dto.RegisterRequestDto;
import com.bank.auth.dto.RegisterResponseDto;
import com.bank.auth.entity.AuthUser;
import com.bank.auth.entity.Token;
import com.bank.auth.jwt.JwtUtils;
import com.bank.auth.repository.AuthRepo;
import com.bank.auth.repository.TokenRepo;
import com.bank.auth.service.AuthService;
import com.bank.common.lib.exception.CommonCustomException;
import com.bank.common.lib.utils.Constants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthRepo authRepo;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EnvironmentParamConfig environmentParamConfig;

    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private TokenRepo tokenRepo;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    public RegisterResponseDto registerNewUser(RegisterRequestDto registerRequestDto) {
        RegisterResponseDto registerResponseDto = null;
        AuthUser authUser = AuthUser.builder()
                .id(UUID.randomUUID().toString())
                .mobNumber(registerRequestDto.getMobNumber())
                .email(registerRequestDto.getEmail())
                .password(bCryptPasswordEncoder.encode(registerRequestDto.getPassword()))
                .role(registerRequestDto.getRole())
                .createdAt(String.valueOf(LocalDateTime.now()))
                .updatedAt(String.valueOf(LocalDateTime.now()))
                .build();
        AuthUser credentials = authRepo.save(authUser);

        // forward user details to user service
        Map<String, Object> userData = new LinkedHashMap<>();
        userData.put("firstName", registerRequestDto.getFirstName());
        userData.put("lastName", registerRequestDto.getLastName());
        userData.put("mobNumber", registerRequestDto.getMobNumber());
        userData.put("email", registerRequestDto.getEmail());
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> request = new HttpEntity<>(userData, headers);

            String url = UriComponentsBuilder
                    .fromUriString(environmentParamConfig.getUserServiceBaseURL())
                    .path("/api/v1/bank/users/create")
                    .build()
                    .toUriString();

            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.POST,
                    request,
                    String.class
            );
            if (response.getStatusCode().value() == 201) {
                String responseBody = response.getBody();
                if (responseBody != null) {
                    JsonObject root = JsonParser.parseString(responseBody).getAsJsonObject();
                    if (root.get("status").getAsString().equalsIgnoreCase("Success") && root.get("statusCode").getAsInt() == 201) {
                        JsonObject payload = root.get("payload").getAsJsonObject();
                        registerResponseDto = new RegisterResponseDto();
                        registerResponseDto.setId(nullStringCheck(payload.get("id").getAsString()));
                        registerResponseDto.setFirstName(nullStringCheck(payload.get("firstName").getAsString()));
                        registerResponseDto.setLastName(nullStringCheck(payload.get("lastName").getAsString()));
                        registerResponseDto.setEmail(nullStringCheck(payload.get("email").getAsString()));
                        registerResponseDto.setMobNumber(nullStringCheck(payload.get("mobNumber").getAsString()));
                        registerResponseDto.setPassword("********");
                        registerResponseDto.setRole(registerRequestDto.getRole());
                    }
                }
            }
        } catch (Exception e) {
            throw new CommonCustomException(Constants.INTERNAL_SERVER_ERROR_STATUS_CODE, e.getMessage());
        }
        return registerResponseDto;
    }

    @Override
    public LoginResponse login(LoginRequestDto loginRequestDto) {
        LoginResponse loginResponse;
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword())
            );
            if (authentication.isAuthenticated()) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                List<String> roles = userDetails.getAuthorities()
                        .stream()
                        .map(GrantedAuthority::getAuthority)
                        .collect(Collectors.toList());

                String jwtToken = jwtUtils.generateToken(loginRequestDto.getEmail(), null, roles);
                String[] parts = jwtToken.split("\\.");
                if (parts.length != 3) {
                    throw new CommonCustomException(Constants.INTERNAL_SERVER_ERROR_STATUS_CODE, "Invalid JWT token");
                }

                String token = new String(Base64.getUrlDecoder().decode(parts[1]));
                JsonObject root = JsonParser.parseString(token).getAsJsonObject();
                Optional<Token> optionalToken = tokenRepo.findBySubject(root.get("sub").getAsString());
                if (optionalToken.isPresent()) {
                    Token existingToken = optionalToken.get();
                    existingToken.setToken(jwtToken);
                    existingToken.setIssuedAt(epochConverter(Long.parseLong(root.get("iat").getAsString())));
                    existingToken.setExpiresAt(epochConverter(Long.parseLong(root.get("exp").getAsString())));
                    existingToken.setRevoked(true);
                    tokenRepo.save(existingToken);
                } else {
                    Token tokenEntity = Token.builder()
                            .id(UUID.randomUUID().toString())
                            .subject(root.get("sub").getAsString())
                            .token(jwtToken)
                            .issuedAt(epochConverter(Long.parseLong(root.get("iat").getAsString())))
                            .expiresAt(epochConverter(Long.parseLong(root.get("exp").getAsString())))
                            .revoked(false)
                            .build();
                    tokenRepo.save(tokenEntity);
                }
                loginResponse = LoginResponse.builder()
                        .tokenType("Bearer")
                        .token(jwtToken)
                        .issuedAt(epochConverter(Long.parseLong(root.get("iat").getAsString())))
                        .expiresAt(epochConverter(Long.parseLong(root.get("exp").getAsString())))
                        .build();
            } else {
                throw new CommonCustomException(Constants.BAD_REQUEST_STATUS_CODE, "Invalid email or password");
            }
        } catch (AuthenticationException ex) {
            throw new CommonCustomException(Constants.BAD_REQUEST_STATUS_CODE, "Invalid email or password");
        }
        return loginResponse;
    }

    private String epochConverter(long epoch) {
        LocalDateTime dateTime = Instant.ofEpochSecond(epoch)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
        return String.valueOf(dateTime);
    }

    private String nullStringCheck(String key) {
        if (key != null && !key.isEmpty()) {
            return key;
        }
        return "";
    }
}
