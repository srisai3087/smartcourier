package com.smartcourier.tracking.controller;

import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.dto.TrackingResponse;
import com.smartcourier.tracking.service.TrackingService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/tracking")
@RequiredArgsConstructor
public class TrackingController {

    private final TrackingService trackingService;


    // for getting all trackig history
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/{trackingNumber}")
    public ResponseEntity<TrackingResponse> getTracking(
            @PathVariable String trackingNumber) {
        return ResponseEntity.ok(
                trackingService.getTrackingByNumber(trackingNumber));
    }


    //creating manual event if not listed
    @PreAuthorize("hasAuthority('ROLE_ADMIN')")
    @PostMapping("/events")
    public ResponseEntity<?> addEvent(
            @RequestBody TrackingEventRequest request,
            @RequestHeader(value = "X-User-Id", defaultValue = "0") Long adminId) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackingService.addEvent(request, adminId));
    }



    //upload proof
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @PostMapping("/documents/upload")
    public ResponseEntity<String> uploadDocument(
            @RequestParam Long deliveryId,
            @RequestParam String documentType,
            @RequestParam MultipartFile file) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(trackingService.uploadDocument(
                        deliveryId, documentType, file));
    }

    //get proof
    @PreAuthorize("hasAnyAuthority('ROLE_CUSTOMER', 'ROLE_ADMIN')")
    @GetMapping("/{deliveryId}/proof")
    public ResponseEntity<?> getProof(
            @PathVariable Long deliveryId) {
        return ResponseEntity.ok(trackingService.getProof(deliveryId));
    }
}
