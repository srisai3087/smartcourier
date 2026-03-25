package com.smartcourier.eureka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * EurekaServerApplication - Netflix Eureka Service Registry
 *
 * This is a SEPARATE module inside the smartcourier project.
 * It runs on port 8761 and acts as the service registry.
 *
 * ALL other services (auth, delivery, tracking, admin, gateway)
 * register themselves here on startup.
 *
 * The gateway uses lb://service-name URLs — Eureka resolves
 * the actual host:port automatically. No hardcoding needed.
 *
 * Dashboard: http://localhost:8761
 *
 * START ORDER:
 *  1. eureka-server  (this service - FIRST)
 *  2. auth-service
 *  3. delivery-service
 *  4. tracking-service
 *  5. admin-service
 *  6. api-gateway    (LAST)
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(EurekaServerApplication.class, args);
    }
}
