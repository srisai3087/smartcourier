package com.smartcourier.admin.config;



import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

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
