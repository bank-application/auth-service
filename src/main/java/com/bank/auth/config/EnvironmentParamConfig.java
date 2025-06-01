package com.bank.auth.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Getter
@Configuration
public class EnvironmentParamConfig {
    @Value("${user.service.baseurl}")
    private String userServiceBaseURL;
}
