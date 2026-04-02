package com.smartcourier.delivery.serviceimpl;

import com.smartcourier.delivery.client.AdminClient;
import com.smartcourier.delivery.client.TrackingServiceClient;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.dto.ExceptionCreateRequest;
import com.smartcourier.delivery.dto.TrackingEventRequest;
import com.smartcourier.delivery.entity.Address;
import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.Package;
import com.smartcourier.delivery.enums.DeliveryStatus;
import com.smartcourier.delivery.exception.InvalidStatusTransitionException;
import com.smartcourier.delivery.exception.ResourceNotFoundException;
import com.smartcourier.delivery.producer.DeliveryEventProducer;
import com.smartcourier.delivery.repository.DeliveryRepository;
import com.smartcourier.delivery.service.DeliveryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {

    private final DeliveryRepository deliveryRepository;

    private final ModelMapper modelMapper;

    // Feign client — auto-calls tracking-service when status changes
    private final TrackingServiceClient trackingServiceClient;

    private final AdminClient adminClient;


    private final DeliveryEventProducer producer;

    @Value("${delivery.charges.domestic-rate}")
    private double domesticRate;

    @Value("${delivery.charges.express-rate}")
    private double expressRate;

    @Value("${delivery.charges.international-rate}")
    private double internationalRate;

    @Value("${delivery.charges.min-domestic}")
    private double minDomestic;

    @Value("${delivery.charges.min-express}")
    private double minExpress;

    @Value("${delivery.charges.min-international}")
    private double minInternational;

    @Value("${delivery.charges.dim-divisor}")
    private double dimDivisor;

    // Valid state transitions for the delivery lifecycle
    private static final Map<DeliveryStatus, Set<DeliveryStatus>> VALID_TRANSITIONS = Map.of(
            DeliveryStatus.DRAFT,            Set.of(DeliveryStatus.BOOKED),
            DeliveryStatus.BOOKED,           Set.of(DeliveryStatus.PICKED_UP),
            DeliveryStatus.PICKED_UP,        Set.of(DeliveryStatus.IN_TRANSIT, DeliveryStatus.DELAYED, DeliveryStatus.FAILED),
            DeliveryStatus.IN_TRANSIT,       Set.of(DeliveryStatus.OUT_FOR_DELIVERY, DeliveryStatus.DELAYED, DeliveryStatus.FAILED),
            DeliveryStatus.OUT_FOR_DELIVERY, Set.of(DeliveryStatus.DELIVERED, DeliveryStatus.FAILED, DeliveryStatus.RETURNED),
            DeliveryStatus.DELIVERED,        Set.of(),
            DeliveryStatus.DELAYED,          Set.of(DeliveryStatus.IN_TRANSIT, DeliveryStatus.RETURNED),
            DeliveryStatus.FAILED,           Set.of(DeliveryStatus.RETURNED),
            DeliveryStatus.RETURNED,         Set.of()
    );

    @Override
    @Transactional
    public DeliveryResponse createDelivery(CreateDeliveryRequest request, Long customerId) {
        log.info("Creating delivery for customerId={}, serviceType={}", customerId, request.getServiceType());

        Address sender   = mapAddress(request.getSenderAddress());
        Address receiver = mapAddress(request.getReceiverAddress());
        Package pkg      = mapPackage(request.getPackageDetails());
        BigDecimal charge = calculateCharge(request.getServiceType(), pkg);

        Delivery delivery = Delivery.builder()
                .trackingNumber(generateTrackingNumber())
                .customerId(customerId)
                .status(DeliveryStatus.DRAFT)
                .serviceType(request.getServiceType().toUpperCase())
                .chargeAmount(charge)
                .scheduledPickup(request.getScheduledPickup())
                .senderAddress(sender)
                .receiverAddress(receiver)
                .parcelPackage(pkg)
                .build();

        Delivery saved = deliveryRepository.save(delivery);

        //  RabbitMQ event (SAFE ADDITION)
        try {
            producer.sendEvent("Delivery Created ID: " + saved.getId());
            log.info("RabbitMQ event sent for deliveryId={}", saved.getId());
        } catch (Exception e) {
            log.warn("RabbitMQ not available: {}", e.getMessage());
        }

        log.info("Delivery created: trackingNumber={}", saved.getTrackingNumber());
        return toResponse(saved);
    }

    @Override
    public List<DeliveryResponse> getMyDeliveries(Long customerId) {
        return deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(customerId)
                .stream().map(this::toResponse).collect(Collectors.toList());
    }

    @Override
    public DeliveryResponse getDeliveryById(Long id) {
        return toResponse(findById(id));
    }

    @Override
    public DeliveryResponse getDeliveryByTracking(String trackingNumber) {
        Delivery delivery = deliveryRepository.findByTrackingNumber(trackingNumber)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Delivery not found with tracking number: " + trackingNumber));
        return toResponse(delivery);
    }

    @Override
    public Page<DeliveryResponse> getAllDeliveries(Pageable pageable) {
        return deliveryRepository.findAll(pageable).map(this::toResponse);
    }

    @Override
    @Transactional
    public DeliveryResponse updateStatus(Long id, DeliveryStatus newStatus) {
        Delivery delivery = findById(id);
        DeliveryStatus previousStatus = delivery.getStatus();

        Set<DeliveryStatus> allowed = VALID_TRANSITIONS.getOrDefault(previousStatus, Set.of());
        if (!allowed.contains(newStatus)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition from %s to %s", previousStatus, newStatus));
        }

        // Update status
        delivery.setStatus(newStatus);
        Delivery saved = deliveryRepository.save(delivery);

        log.info("Delivery {} status: {} -> {}", id, previousStatus, newStatus);

        autoCreateTrackingEvent(saved, previousStatus, newStatus);


        // AUTO EXCEPTION CREATION (ADD THIS BLOCK)
        if (newStatus == DeliveryStatus.FAILED || newStatus == DeliveryStatus.DELAYED) {
            try {
                ExceptionCreateRequest req = new ExceptionCreateRequest();
                req.setDeliveryId(saved.getId());
                req.setExceptionType(newStatus.name());
                req.setReason("Auto-generated due to delivery issue");

                adminClient.createException(req);

                log.info("Exception created for deliveryId={}", saved.getId());
            } catch (Exception e) {
                log.warn("Failed to create exception: {}", e.getMessage());
            }
        }
        return toResponse(saved);
    }

    private void autoCreateTrackingEvent(Delivery delivery, DeliveryStatus from, DeliveryStatus to) {
        try {
            TrackingEventRequest event = TrackingEventRequest.builder()
                    .deliveryId(delivery.getId())
                    .trackingNumber(delivery.getTrackingNumber())
                    .eventType(to.name())
                    .location("Mumbai Central Hub")
                    .remarks("Status changed: " + from.name() + " -> " + to.name())
                    .eventTime(LocalDateTime.now())
                    .build();
            trackingServiceClient.addEvent(event);
            log.info("Tracking event sent via Feign: deliveryId={}, event={}", delivery.getId(), to.name());
        } catch (Exception e) {
            log.warn("Could not push tracking event (tracking-service may be down): {}", e.getMessage());
        }
    }

    private BigDecimal calculateCharge(String serviceType, Package pkg) {
        double actual     = pkg.getWeightKg();
        double volumetric = 0;

        if (pkg.getLengthCm() != null && pkg.getWidthCm() != null && pkg.getHeightCm() != null) {
            volumetric = (pkg.getLengthCm() * pkg.getWidthCm() * pkg.getHeightCm()) / dimDivisor;
        }

        double billable = Math.max(actual, volumetric);
        double rate, minimum;

        switch (serviceType.toUpperCase()) {
            case "EXPRESS":
                rate = expressRate; minimum = minExpress; break;
            case "INTERNATIONAL":
                rate = internationalRate; minimum = minInternational; break;
            default:
                rate = domesticRate; minimum = minDomestic;
        }

        double charge = Math.max(billable * rate, minimum);
        return BigDecimal.valueOf(charge).setScale(2, RoundingMode.HALF_UP);
    }

    private String generateTrackingNumber() {
        return "SC-" + UUID.randomUUID().toString().toUpperCase().substring(0, 10);
    }

    private Delivery findById(Long id) {
        return deliveryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Delivery not found with id: " + id));
    }

    private Address mapAddress(CreateDeliveryRequest.AddressRequest dto) {
        return modelMapper.map(dto, Address.class);
    }

    private Package mapPackage(CreateDeliveryRequest.PackageRequest dto) {
        Package pkg = modelMapper.map(dto, Package.class);
        if (pkg.getIsFragile() == null) pkg.setIsFragile(false);
        return pkg;
    }

    private DeliveryResponse toResponse(Delivery d) {

        DeliveryResponse response = new DeliveryResponse();

        // SAFE mapping
        modelMapper.map(d, response);

        if (d.getSenderAddress() != null) {
            response.setSenderCity(d.getSenderAddress().getCity());
            response.setSenderState(d.getSenderAddress().getState());
        }

        if (d.getReceiverAddress() != null) {
            response.setReceiverCity(d.getReceiverAddress().getCity());
            response.setReceiverState(d.getReceiverAddress().getState());
            response.setReceiverName(d.getReceiverAddress().getFullName());
        }

        if (d.getParcelPackage() != null) {
            response.setWeightKg(d.getParcelPackage().getWeightKg());
            response.setIsFragile(d.getParcelPackage().getIsFragile());
        }

        return response;
    }


}
