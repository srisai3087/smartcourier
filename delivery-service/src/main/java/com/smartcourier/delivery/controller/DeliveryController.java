package com.smartcourier.delivery.controller;

import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.enums.DeliveryStatus;
import com.smartcourier.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/deliveries")
@RequiredArgsConstructor
public class DeliveryController {

    private final DeliveryService deliveryService;

    // CUSTOMER and ADMIN both can create delivery
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @PostMapping
    public ResponseEntity<DeliveryResponse> createDelivery(
            @RequestBody CreateDeliveryRequest request,
            @RequestHeader("X-User-Id") Long customerId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(deliveryService.createDelivery(request, customerId));
    }

    // CUSTOMER sees only their own deliveries
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/my")
    public ResponseEntity<List<DeliveryResponse>> getMyDeliveries(
            @RequestHeader("X-User-Id") Long customerId) {
        return ResponseEntity.ok(deliveryService.getMyDeliveries(customerId));
    }

    // Both can view single delivery
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/{id}")
    public ResponseEntity<DeliveryResponse> getDeliveryById(
            @PathVariable Long id) {
        return ResponseEntity.ok(deliveryService.getDeliveryById(id));
    }

    // Both can track by number
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/track/{trackingNumber}")
    public ResponseEntity<DeliveryResponse> trackDelivery(
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(deliveryService.getDeliveryByTracking(trackingNumber));
    }

    // ONLY ADMIN can update status
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PatchMapping("/{id}/status")
    public ResponseEntity<DeliveryResponse> updateStatus(
            @PathVariable Long id,
            @RequestParam DeliveryStatus status) {
        return ResponseEntity.ok(deliveryService.updateStatus(id, status));
    }

    // ONLY ADMIN can see all deliveries
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @GetMapping
    public ResponseEntity<Page<DeliveryResponse>> getAllDeliveries(
            Pageable pageable) {
        return ResponseEntity.ok(deliveryService.getAllDeliveries(pageable));
    }
}