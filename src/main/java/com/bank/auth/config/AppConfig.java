package com.bank.auth.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.web.client.ExtractingResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {

    @Bean
    public ObjectMapper objectMapper(){
        return new ObjectMapper();
    }

    @Bean
    public RestTemplate restTemplate(){
        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setRequestFactory(clientHttpRequestFactory());
        restTemplate.setErrorHandler(new ExtractingResponseErrorHandler());
        return restTemplate;
    }

    private ClientHttpRequestFactory clientHttpRequestFactory(){
        HttpComponentsClientHttpRequestFactory httpComponentsClientHttpRequestFactory = new HttpComponentsClientHttpRequestFactory();
        httpComponentsClientHttpRequestFactory.setConnectTimeout(1000 * 30); // Time to establish connection
        httpComponentsClientHttpRequestFactory.setReadTimeout(1000 * 60); // Time to wait for response data
        return httpComponentsClientHttpRequestFactory;
    }
}
