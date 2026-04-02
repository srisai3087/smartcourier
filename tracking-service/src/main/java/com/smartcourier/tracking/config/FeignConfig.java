package com.smartcourier.tracking.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

// Adds internal service headers on every Feign call so @PreAuthorize checks pass.
@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {
            template.header("X-User-Id", "0");
            template.header("X-User-Role", "ROLE_ADMIN");
        };
    }
}
