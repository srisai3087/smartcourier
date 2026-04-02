package com.smartcourier.tracking.serviceimpl;

import com.smartcourier.tracking.client.DeliveryServiceClient;
import com.smartcourier.tracking.dto.ProofResponse;
import com.smartcourier.tracking.dto.TrackingEventRequest;
import com.smartcourier.tracking.dto.TrackingResponse;
import com.smartcourier.tracking.entity.DeliveryProof;
import com.smartcourier.tracking.entity.Document;
import com.smartcourier.tracking.entity.TrackingEvent;
import com.smartcourier.tracking.exception.ResourceNotFoundException;
import com.smartcourier.tracking.repository.DeliveryProofRepository;
import com.smartcourier.tracking.repository.DocumentRepository;
import com.smartcourier.tracking.repository.TrackingEventRepository;
import com.smartcourier.tracking.service.TrackingService;
import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class TrackingServiceable implements TrackingService {

    private final TrackingEventRepository eventRepository;
    private final DocumentRepository documentRepository;
    private final DeliveryProofRepository proofRepository;
    private final DeliveryServiceClient deliveryServiceClient;
    private final ModelMapper modelMapper;

    @Value("${file.upload-dir}")
    private String uploadDir;

    @Override
    public TrackingResponse getTrackingByNumber(String trackingNumber) {
        List<TrackingEvent> events = eventRepository.findByTrackingNumberOrderByEventTimeDesc(trackingNumber);
        if (events.isEmpty()) {
            throw new ResourceNotFoundException("No tracking events found for: " + trackingNumber);
        }
        List<TrackingResponse.EventDTO> dtos = events.stream()
                .map(e -> modelMapper.map(e, TrackingResponse.EventDTO.class))
                .collect(Collectors.toList());
        return TrackingResponse.builder()
                .trackingNumber(trackingNumber)
                .deliveryId(events.get(0).getDeliveryId())
                .events(dtos)
                .build();
    }

    @Override
    @Transactional
    public TrackingResponse.EventDTO addEvent(TrackingEventRequest req, Long adminId) {
        try {
            deliveryServiceClient.getDeliveryById(req.getDeliveryId());
            log.info("Delivery {} verified via Feign", req.getDeliveryId());
        } catch (FeignException.NotFound e) {
            throw new ResourceNotFoundException("Delivery not found with id: " + req.getDeliveryId());
        } catch (Exception e) {
            log.warn("Could not verify delivery via Feign: {}", e.getMessage());
        }

        TrackingEvent event = new TrackingEvent();
        event.setDeliveryId(req.getDeliveryId());
        event.setTrackingNumber(req.getTrackingNumber());
        event.setEventType(req.getEventType());
        event.setLocation(req.getLocation());
        event.setRemarks(req.getRemarks());
        event.setEventTime(req.getEventTime());
        event.setCreatedBy(adminId != null ? adminId : 0L);

        TrackingEvent saved = eventRepository.save(event);
        log.info("Tracking event '{}' added for: {}", req.getEventType(), req.getTrackingNumber());
        return modelMapper.map(saved, TrackingResponse.EventDTO.class);
    }

    @Override
    @Transactional
    public String uploadDocument(Long deliveryId, String documentType, MultipartFile file) {
        try {
            Path dir = Paths.get(uploadDir, deliveryId.toString());
            Files.createDirectories(dir);
            Path filePath = dir.resolve(file.getOriginalFilename());
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            Document doc = Document.builder()
                    .deliveryId(deliveryId)
                    .documentType(documentType.toUpperCase())
                    .fileName(file.getOriginalFilename())
                    .storagePath(filePath.toString())
                    .fileSizeBytes(file.getSize())
                    .build();
            documentRepository.save(doc);
            log.info("Document '{}' uploaded for deliveryId={}", file.getOriginalFilename(), deliveryId);
            return filePath.toString();
        } catch (IOException e) {
            throw new RuntimeException("Failed to store file: " + e.getMessage());
        }
    }

    @Override
    public ProofResponse getProof(Long deliveryId) {

        List<Document> docs = documentRepository.findByDeliveryId(deliveryId);

        if (docs.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No documents found for deliveryId: " + deliveryId);
        }

        Document doc = docs.get(0);

        ProofResponse response = new ProofResponse();

        response.setDeliveryId(deliveryId);
        response.setPhotoPath(doc.getStoragePath());
        response.setRemarks("Document uploaded");

        return response;
    }
}