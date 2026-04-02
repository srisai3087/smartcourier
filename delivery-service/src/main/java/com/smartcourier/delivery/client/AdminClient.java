package com.smartcourier.delivery.client;

import com.smartcourier.delivery.dto.ExceptionCreateRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "admin-service")
public interface AdminClient {

    @PostMapping("/admin/exceptions")
    void createException(@RequestBody ExceptionCreateRequest request);
}