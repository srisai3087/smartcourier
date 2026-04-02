package com.smartcourier.delivery;


import com.smartcourier.delivery.client.AdminClient;
import com.smartcourier.delivery.client.TrackingServiceClient;
import com.smartcourier.delivery.dto.CreateDeliveryRequest;
import com.smartcourier.delivery.dto.DeliveryResponse;
import com.smartcourier.delivery.entity.Address;
import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.entity.Package;
import com.smartcourier.delivery.enums.DeliveryStatus;
import com.smartcourier.delivery.exception.InvalidStatusTransitionException;
import com.smartcourier.delivery.exception.ResourceNotFoundException;
import com.smartcourier.delivery.producer.DeliveryEventProducer;
import com.smartcourier.delivery.repository.DeliveryRepository;
import com.smartcourier.delivery.serviceimpl.DeliveryServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DeliveryServiceImplTest {

    // ── MOCKS ──────────────────────────────────────────────────────────────────
    @Mock
    private DeliveryRepository deliveryRepository;

    @Mock
    private ModelMapper modelMapper;

    @Mock
    private TrackingServiceClient trackingServiceClient;

    @Mock
    private AdminClient adminClient;

    @Mock
    private DeliveryEventProducer producer;

    // ── REAL CLASS BEING TESTED ────────────────────────────────────────────────
    @InjectMocks
    private DeliveryServiceImpl deliveryService;

    // ── TEST DATA ──────────────────────────────────────────────────────────────
    private Delivery mockDelivery;
    private CreateDeliveryRequest createRequest;
    private Address senderAddress;
    private Address receiverAddress;
    private Package mockPackage;

    @BeforeEach
    void setUp() {
        // Inject @Value fields manually — Spring is not running in unit tests
        ReflectionTestUtils.setField(deliveryService, "domesticRate", 50.0);
        ReflectionTestUtils.setField(deliveryService, "expressRate", 100.0);
        ReflectionTestUtils.setField(deliveryService, "internationalRate", 250.0);
        ReflectionTestUtils.setField(deliveryService, "minDomestic", 100.0);
        ReflectionTestUtils.setField(deliveryService, "minExpress", 200.0);
        ReflectionTestUtils.setField(deliveryService, "minInternational", 500.0);
        ReflectionTestUtils.setField(deliveryService, "dimDivisor", 5000.0);

        // Build sender address entity
        senderAddress = new Address();
        senderAddress.setFullName("Rahul Sharma");
        senderAddress.setStreet("123 MG Road");
        senderAddress.setCity("Mumbai");
        senderAddress.setState("Maharashtra");
        senderAddress.setPinCode("400001");
        senderAddress.setCountry("India");
        senderAddress.setPhone("9876543210");

        // Build receiver address entity
        receiverAddress = new Address();
        receiverAddress.setFullName("Priya Patel");
        receiverAddress.setStreet("456 Park Street");
        receiverAddress.setCity("Delhi");
        receiverAddress.setState("Delhi");
        receiverAddress.setPinCode("110001");
        receiverAddress.setCountry("India");
        receiverAddress.setPhone("8765432109");

        // Build package entity
        mockPackage = new Package();
        mockPackage.setWeightKg(2.5);
        mockPackage.setLengthCm(30.0);
        mockPackage.setWidthCm(20.0);
        mockPackage.setHeightCm(15.0);
        mockPackage.setIsFragile(true);
        mockPackage.setDescription("Laptop");

        // Build mock delivery entity
        mockDelivery = new Delivery();
        mockDelivery.setId(1L);
        mockDelivery.setTrackingNumber("SC-A1B2C3D4E5");
        mockDelivery.setCustomerId(1L);
        mockDelivery.setStatus(DeliveryStatus.DRAFT);
        mockDelivery.setServiceType("DOMESTIC");
        mockDelivery.setChargeAmount(new BigDecimal("125.00"));
        mockDelivery.setSenderAddress(senderAddress);
        mockDelivery.setReceiverAddress(receiverAddress);
        mockDelivery.setParcelPackage(mockPackage);

        // Build sender address request
        CreateDeliveryRequest.AddressRequest senderReq =
                new CreateDeliveryRequest.AddressRequest();
        senderReq.setFullName("Rahul Sharma");
        senderReq.setStreet("123 MG Road");
        senderReq.setCity("Mumbai");
        senderReq.setState("Maharashtra");
        senderReq.setPinCode("400001");
        senderReq.setCountry("India");
        senderReq.setPhone("9876543210");

        // Build receiver address request
        CreateDeliveryRequest.AddressRequest receiverReq =
                new CreateDeliveryRequest.AddressRequest();
        receiverReq.setFullName("Priya Patel");
        receiverReq.setStreet("456 Park Street");
        receiverReq.setCity("Delhi");
        receiverReq.setState("Delhi");
        receiverReq.setPinCode("110001");
        receiverReq.setCountry("India");
        receiverReq.setPhone("8765432109");

        // Build package request
        CreateDeliveryRequest.PackageRequest pkgReq =
                new CreateDeliveryRequest.PackageRequest();
        pkgReq.setWeightKg(2.5);
        pkgReq.setLengthCm(30.0);
        pkgReq.setWidthCm(20.0);
        pkgReq.setHeightCm(15.0);
        pkgReq.setIsFragile(true);
        pkgReq.setDescription("Laptop");



// Build create delivery request (FINAL FIX)
        createRequest = CreateDeliveryRequest.builder()
                .serviceType("DOMESTIC")
                .senderAddress(senderReq)
                .receiverAddress(receiverReq)
                .packageDetails(pkgReq)
                .build();
    }

    // ── CREATE DELIVERY TESTS ──────────────────────────────────────────────────

    @Test
    void createDelivery_success_shouldReturnDeliveryResponse() {
        // ModelMapper maps DTO to entity
        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(mockPackage);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        // ModelMapper maps entity to response
        doAnswer(invocation -> {
            DeliveryResponse response = invocation.getArgument(1);
            response.setId(1L);
            response.setTrackingNumber("SC-A1B2C3D4E5");
            response.setStatus(DeliveryStatus.DRAFT);
            response.setCustomerId(1L);
            response.setChargeAmount(new BigDecimal("125.00"));
            return null;
        }).when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        DeliveryResponse response = deliveryService.createDelivery(createRequest, 1L);

        assertNotNull(response);
        assertEquals("SC-A1B2C3D4E5", response.getTrackingNumber());
        assertEquals(DeliveryStatus.DRAFT, response.getStatus());
        assertEquals(1L, response.getCustomerId());

        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void createDelivery_shouldStartWithDraftStatus() {
        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(mockPackage);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        DeliveryResponse response = deliveryService.createDelivery(createRequest, 1L);

        // Verify save was called with DRAFT status
        verify(deliveryRepository).save(argThat(d ->
                d.getStatus() == DeliveryStatus.DRAFT
        ));
    }

    @Test
    void createDelivery_whenRepositorySaveFails_shouldThrowException() {
        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(mockPackage);
        when(deliveryRepository.save(any(Delivery.class)))
                .thenThrow(new RuntimeException("DB Error"));

        assertThrows(RuntimeException.class,
                () -> deliveryService.createDelivery(createRequest, 1L));
    }

    @Test
    void createDelivery_whenRabbitMQDown_shouldStillSucceed() {
        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(mockPackage);
        when(deliveryRepository.save(any(Delivery.class))).thenReturn(mockDelivery);

        // RabbitMQ throws exception
        doThrow(new RuntimeException("RabbitMQ down"))
                .when(producer).sendEvent(any());

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        // Delivery creation should still succeed even if RabbitMQ is down
        assertDoesNotThrow(
                () -> deliveryService.createDelivery(createRequest, 1L));
    }

    // ── UPDATE STATUS TESTS ────────────────────────────────────────────────────

    @Test
    void updateStatus_validTransition_DRAFT_to_BOOKED_shouldSucceed() {
        Delivery bookedDelivery = new Delivery();
        bookedDelivery.setId(1L);
        bookedDelivery.setTrackingNumber("SC-A1B2C3D4E5");
        bookedDelivery.setStatus(DeliveryStatus.BOOKED);
        bookedDelivery.setCustomerId(1L);
        bookedDelivery.setChargeAmount(new BigDecimal("125.00"));
        bookedDelivery.setSenderAddress(senderAddress);
        bookedDelivery.setReceiverAddress(receiverAddress);
        bookedDelivery.setParcelPackage(mockPackage);

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenReturn(bookedDelivery);

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        DeliveryResponse response = deliveryService.updateStatus(1L, DeliveryStatus.BOOKED);

        verify(deliveryRepository).save(any(Delivery.class));
    }

    @Test
    void updateStatus_invalidTransition_DRAFT_to_DELIVERED_shouldThrow422() {
        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));

        assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus(1L, DeliveryStatus.DELIVERED));

        // Save must never be called when transition is invalid
        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void updateStatus_deliveryNotFound_shouldThrow404() {
        when(deliveryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deliveryService.updateStatus(999L, DeliveryStatus.BOOKED));
    }

    @Test
    void updateStatus_toFailed_shouldAutoCreateException() {
        mockDelivery.setStatus(DeliveryStatus.IN_TRANSIT);

        Delivery failedDelivery = new Delivery();
        failedDelivery.setId(1L);
        failedDelivery.setStatus(DeliveryStatus.FAILED);
        failedDelivery.setTrackingNumber("SC-A1B2C3D4E5");
        failedDelivery.setSenderAddress(senderAddress);
        failedDelivery.setReceiverAddress(receiverAddress);
        failedDelivery.setParcelPackage(mockPackage);
        failedDelivery.setChargeAmount(new BigDecimal("125.00"));

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenReturn(failedDelivery);

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        deliveryService.updateStatus(1L, DeliveryStatus.FAILED);

        // AdminClient must be called to create exception
        verify(adminClient).createException(any());
    }

    @Test
    void updateStatus_toDelayed_shouldAutoCreateException() {
        mockDelivery.setStatus(DeliveryStatus.IN_TRANSIT);

        Delivery delayedDelivery = new Delivery();
        delayedDelivery.setId(1L);
        delayedDelivery.setStatus(DeliveryStatus.DELAYED);
        delayedDelivery.setTrackingNumber("SC-A1B2C3D4E5");
        delayedDelivery.setSenderAddress(senderAddress);
        delayedDelivery.setReceiverAddress(receiverAddress);
        delayedDelivery.setParcelPackage(mockPackage);
        delayedDelivery.setChargeAmount(new BigDecimal("125.00"));

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenReturn(delayedDelivery);

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        deliveryService.updateStatus(1L, DeliveryStatus.DELAYED);

        // AdminClient must be called to create exception
        verify(adminClient).createException(any());
    }

    @Test
    void updateStatus_adminClientDown_shouldStillUpdateStatus() {
        mockDelivery.setStatus(DeliveryStatus.IN_TRANSIT);

        Delivery failedDelivery = new Delivery();
        failedDelivery.setId(1L);
        failedDelivery.setStatus(DeliveryStatus.FAILED);
        failedDelivery.setTrackingNumber("SC-A1B2C3D4E5");
        failedDelivery.setSenderAddress(senderAddress);
        failedDelivery.setReceiverAddress(receiverAddress);
        failedDelivery.setParcelPackage(mockPackage);
        failedDelivery.setChargeAmount(new BigDecimal("125.00"));

        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));
        when(deliveryRepository.save(any(Delivery.class)))
                .thenReturn(failedDelivery);
        doThrow(new RuntimeException("Admin service down"))
                .when(adminClient).createException(any());

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        // Status update succeeds even if admin service is down
        assertDoesNotThrow(
                () -> deliveryService.updateStatus(1L, DeliveryStatus.FAILED));

        verify(deliveryRepository).save(any());
    }

    // ── GET DELIVERY TESTS ─────────────────────────────────────────────────────

    @Test
    void getDeliveryById_found_shouldReturnResponse() {
        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        DeliveryResponse response = deliveryService.getDeliveryById(1L);

        assertNotNull(response);
        verify(deliveryRepository).findById(1L);
    }

    @Test
    void getDeliveryById_notFound_shouldThrowResourceNotFoundException() {
        when(deliveryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deliveryService.getDeliveryById(999L));
    }

    @Test
    void getDeliveryByTracking_found_shouldReturnResponse() {
        when(deliveryRepository.findByTrackingNumber("SC-A1B2C3D4E5"))
                .thenReturn(Optional.of(mockDelivery));

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        DeliveryResponse response = deliveryService
                .getDeliveryByTracking("SC-A1B2C3D4E5");

        assertNotNull(response);
        verify(deliveryRepository).findByTrackingNumber("SC-A1B2C3D4E5");
    }

    @Test
    void getDeliveryByTracking_notFound_shouldThrowResourceNotFoundException() {
        when(deliveryRepository.findByTrackingNumber("SC-NOTFOUND"))
                .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> deliveryService.getDeliveryByTracking("SC-NOTFOUND"));
    }

    @Test
    void getMyDeliveries_shouldReturnOnlyCustomerDeliveries() {
        when(deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(1L))
                .thenReturn(List.of(mockDelivery));

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        List<DeliveryResponse> responses = deliveryService.getMyDeliveries(1L);

        assertNotNull(responses);
        assertEquals(1, responses.size());
        verify(deliveryRepository).findByCustomerIdOrderByCreatedAtDesc(1L);
    }

    @Test
    void getMyDeliveries_noDeliveries_shouldReturnEmptyList() {
        when(deliveryRepository.findByCustomerIdOrderByCreatedAtDesc(999L))
                .thenReturn(List.of());

        List<DeliveryResponse> responses = deliveryService.getMyDeliveries(999L);

        assertNotNull(responses);
        assertTrue(responses.isEmpty());
    }

    @Test
    void getAllDeliveries_shouldReturnPagedResults() {
        Page<Delivery> page = new PageImpl<>(List.of(mockDelivery));
        when(deliveryRepository.findAll(any(PageRequest.class))).thenReturn(page);

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        Page<DeliveryResponse> result = deliveryService
                .getAllDeliveries(PageRequest.of(0, 10));

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
    }

    // ── CHARGE CALCULATION TESTS ───────────────────────────────────────────────

    @Test
    void createDelivery_domesticCharge_shouldBeCalculatedCorrectly() {
        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(mockPackage);

        // Capture what was passed to save
        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            // actual=2.5, volumetric=(30x20x15)/5000=1.8, billable=2.5, charge=2.5x50=125
            assertEquals(0, new BigDecimal("125.00").compareTo(d.getChargeAmount()));
            mockDelivery.setChargeAmount(d.getChargeAmount());
            return mockDelivery;
        });

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        deliveryService.createDelivery(createRequest, 1L);
    }

    @Test
    void createDelivery_expressCharge_shouldApplyExpressRate() {
        createRequest.setServiceType("EXPRESS");

        Package expressPackage = new Package();
        expressPackage.setWeightKg(1.0);
        expressPackage.setLengthCm(10.0);
        expressPackage.setWidthCm(10.0);
        expressPackage.setHeightCm(10.0);
        expressPackage.setIsFragile(false);

        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(expressPackage);

        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            // weight=1.0, volumetric=(10x10x10)/5000=0.2, billable=1.0, rate=100, charge=max(100,200)=200
            assertEquals(0, new BigDecimal("200.00").compareTo(d.getChargeAmount()));
            mockDelivery.setChargeAmount(d.getChargeAmount());
            return mockDelivery;
        });

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        deliveryService.createDelivery(createRequest, 1L);
    }

    @Test
    void createDelivery_minimumCharge_shouldApplyMinimumWhenChargeIsLow() {
        // Very light package — charge would be below minimum
        Package lightPackage = new Package();
        lightPackage.setWeightKg(0.1);
        lightPackage.setIsFragile(false);

        when(modelMapper.map(any(CreateDeliveryRequest.AddressRequest.class),
                eq(Address.class))).thenReturn(senderAddress);
        when(modelMapper.map(any(CreateDeliveryRequest.PackageRequest.class),
                eq(Package.class))).thenReturn(lightPackage);

        when(deliveryRepository.save(any(Delivery.class))).thenAnswer(invocation -> {
            Delivery d = invocation.getArgument(0);
            // weight=0.1, charge=0.1x50=5 but minimum=100 so charge=100
            assertEquals(0, new BigDecimal("100.00").compareTo(d.getChargeAmount()));
            mockDelivery.setChargeAmount(d.getChargeAmount());
            return mockDelivery;
        });

        doAnswer(invocation -> null)
                .when(modelMapper).map(any(Delivery.class), any(DeliveryResponse.class));

        deliveryService.createDelivery(createRequest, 1L);
    }

    // ── STATE MACHINE BOUNDARY TESTS ───────────────────────────────────────────

    @Test
    void updateStatus_DELIVERED_toAnyStatus_shouldThrowException() {
        mockDelivery.setStatus(DeliveryStatus.DELIVERED);
        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));

        // DELIVERED is end state — no transitions allowed
        assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus(1L, DeliveryStatus.RETURNED));

        verify(deliveryRepository, never()).save(any());
    }

    @Test
    void updateStatus_RETURNED_toAnyStatus_shouldThrowException() {
        mockDelivery.setStatus(DeliveryStatus.RETURNED);
        when(deliveryRepository.findById(1L))
                .thenReturn(Optional.of(mockDelivery));

        // RETURNED is end state — no transitions allowed
        assertThrows(InvalidStatusTransitionException.class,
                () -> deliveryService.updateStatus(1L, DeliveryStatus.IN_TRANSIT));

        verify(deliveryRepository, never()).save(any());
    }
}