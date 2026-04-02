package com.smartcourier.delivery.service;

import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DeliveryService {

    DeliveryResponse createDelivery(CreateDeliveryRequest request, Long customerId);

    List<DeliveryResponse> getMyDeliveries(Long customerId);

    DeliveryResponse getDeliveryById(Long id);

    DeliveryResponse updateStatus(Long id, DeliveryStatus newStatus);

    Page<DeliveryResponse> getAllDeliveries(Pageable pageable);

    DeliveryResponse getDeliveryByTracking(String trackingNumber);


}
