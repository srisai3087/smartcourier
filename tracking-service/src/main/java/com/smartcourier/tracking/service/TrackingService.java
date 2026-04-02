package com.smartcourier.tracking.service;
import com.smartcourier.tracking.dto.*;
import org.springframework.web.multipart.MultipartFile;

public interface TrackingService {
    TrackingResponse getTrackingByNumber(String trackingNumber);
    TrackingResponse.EventDTO addEvent(TrackingEventRequest request, Long adminId);
    String uploadDocument(Long deliveryId, String documentType, MultipartFile file);
    ProofResponse getProof(Long deliveryId);
}
