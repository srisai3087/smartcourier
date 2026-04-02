package com.smartcourier.admin.client;



import com.smartcourier.admin.dto.DeliveryDTO;
import com.smartcourier.admin.dto.PageResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
@FeignClient(name = "delivery-service")
public interface DeliveryServiceClient {

    @GetMapping("/deliveries")
    PageResponse<DeliveryDTO> getAllDeliveries(
            @RequestParam("page") int page,
            @RequestParam("size") int size
    );
}