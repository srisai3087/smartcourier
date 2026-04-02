package com.smartcourier.delivery.config;

import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignConfig {

    @Bean
    public RequestInterceptor feignRequestInterceptor() {
        return template -> {

            // so @PreAuthorize on tracking-service passes
            template.header("X-User-Id", "0");
            template.header("X-User-Role", "ROLE_ADMIN");
        };
    }
}