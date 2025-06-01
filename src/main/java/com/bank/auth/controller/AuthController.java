package com.bank.auth.controller;

import com.bank.auth.config.EnvironmentParamConfig;
import com.bank.auth.dto.LoginRequestDto;
import com.bank.auth.dto.LoginResponse;
import com.bank.auth.dto.RegisterRequestDto;
import com.bank.auth.dto.RegisterResponseDto;
import com.bank.auth.jwt.JwtUtils;
import com.bank.auth.service.AuthService;
import com.bank.common.lib.exception.CommonCustomException;
import com.bank.common.lib.model.response.CommonSuccessResponse;
import com.bank.common.lib.utils.Constants;
import com.bank.common.lib.utils.MetadataContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/bank/auth")
public class AuthController {

    @Autowired
    private JwtUtils jwtUtils;

    @Autowired
    private AuthService authService;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private EnvironmentParamConfig environmentParamConfig;

    @PostMapping("/register")
    public ResponseEntity<CommonSuccessResponse<RegisterResponseDto>> register(@RequestBody RegisterRequestDto registerRequestDto) {
        RegisterResponseDto registeredUser = authService.registerNewUser(registerRequestDto);
        return getSpecificResponse("New user registration successfully", Constants.CREATED_STATUS_CODE, registeredUser);
    }

    @PostMapping("/login")
    public ResponseEntity<CommonSuccessResponse<LoginResponse>> login(@RequestBody LoginRequestDto loginRequestDto) {
        LoginResponse loginResponse = authService.login(loginRequestDto);
        return getSpecificResponse("Login successful", Constants.OK_STATUS_CODE, loginResponse);
    }

    @GetMapping("/status")
    public ResponseEntity<CommonSuccessResponse<Map<String, Object>>> getStatus() {
        Map<String, Object> status = new LinkedHashMap<>();
        Map<String, Object> components = new LinkedHashMap<>();

        boolean tokenServiceUp = testTokenService();
        boolean mongoUp = isMongoAlive();
        boolean userServiceUp = isUserServiceAlive();

        components.put("token-service", tokenServiceUp ? "UP" : "DOWN");
        components.put("user-service", userServiceUp ? "UP" : "DOWN");
        components.put("mongodb", mongoUp ? "UP" : "DOWN");

        long upCount = components.values().stream().filter("UP"::equals).count();
        long total = components.size();

        String overallStatus;
        if (upCount == total) {
            overallStatus = "UP";
        } else if (upCount == 0) {
            overallStatus = "DOWN";
        } else {
            overallStatus = "DEGRADED";
        }

        status.put("status", overallStatus);
        status.put("uptime", getUptime());
        status.put("components", components);
        return getSpecificResponse("Service status fetched successfully", Constants.OK_STATUS_CODE, status);
    }

    public boolean isUserServiceAlive() {
        try {
            String url = UriComponentsBuilder
                    .fromUriString(environmentParamConfig.getUserServiceBaseURL())
                    .path("/actuator/health")
                    .build()
                    .toUriString();
            ResponseEntity<String> response = restTemplate.getForEntity(url, String.class);
            return response.getStatusCode().is2xxSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isMongoAlive() {
        try {
            mongoTemplate.getDb().listCollectionNames().first(); // ping MongoDB
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean testTokenService() {
        try {
            String dummyToken = jwtUtils.generateToken("healthcheck@gmail.com", null, List.of("USER, ADMIN"));
            return jwtUtils.isTokenValid(dummyToken);
        } catch (Exception e) {
            return false;
        }
    }

    private String getUptime() {
        RuntimeMXBean runtimeMxBean = ManagementFactory.getRuntimeMXBean();
        long uptimeMillis = runtimeMxBean.getUptime();

        Duration duration = Duration.ofMillis(uptimeMillis);
        long hours = duration.toHours();
        long minutes = duration.toMinutes() % 60;
        long seconds = duration.getSeconds() % 60;
        return String.format("%02dh:%02dm:%02ds", hours, minutes, seconds);
    }

    private <T> ResponseEntity<CommonSuccessResponse<T>> getSpecificResponse(String msg, int statusCode, T payload) {
        try {
            CommonSuccessResponse<T> response = CommonSuccessResponse.<T>builder()
                    .timestamp(String.valueOf(LocalDateTime.now()))
                    .status(Constants.SUCCESS_TAG)
                    .statusCode(statusCode)
                    .message(msg)
                    .metadata(MetadataContext.getMetadata())
                    .payload(payload)
                    .build();
            return ResponseEntity.status(statusCode).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            throw new CommonCustomException(Constants.INTERNAL_SERVER_ERROR_STATUS_CODE, e.getMessage());
        }
    }
}
