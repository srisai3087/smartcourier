package com.smartcourier.tracking.client;

import com.smartcourier.tracking.config.FeignConfig;
import com.smartcourier.tracking.dto.DeliveryResponseDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

// Feign client for delivery-service. Eureka resolves the actual host:port automatically.
@FeignClient(name = "delivery-service", configuration = FeignConfig.class)
public interface DeliveryServiceClient {

    @GetMapping("/deliveries/{id}")
    DeliveryResponseDto getDeliveryById(@PathVariable("id") Long id);
}
