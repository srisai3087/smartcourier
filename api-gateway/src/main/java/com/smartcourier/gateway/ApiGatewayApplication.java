package com.smartcourier.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;



/**
 * SmartCourier API Gateway - Entry Point
 *
 * This is the SINGLE entry point for all client requests.
 * Routes traffic to appropriate microservices after JWT validation.
 *
 * Spring Modules Used:
 *  - Spring Cloud Gateway (WebFlux-based reactive gateway)
 *  - Spring Boot Autoconfiguration
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
