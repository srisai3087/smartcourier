package com.smartcourier.delivery.client;

import com.smartcourier.delivery.config.FeignConfig;
import com.smartcourier.delivery.dto.TrackingEventRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "tracking-service",
        configuration = FeignConfig.class
)
public interface TrackingServiceClient {

    @PostMapping("/tracking/events")
    Object addEvent(@RequestBody TrackingEventRequest request);  // ← changed void to Object
}