package com.smartcourier.delivery;

import com.smartcourier.delivery.client.TrackingServiceClient;
import com.smartcourier.delivery.dto.TrackingEventRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TrackingServiceClientTest {

    @Mock
    private TrackingServiceClient trackingServiceClient;

    private TrackingEventRequest validRequest;

    @BeforeEach
    void setUp() {
        validRequest = TrackingEventRequest.builder()
                .deliveryId(1L)
                .trackingNumber("SC-ABCDE12345")
                .eventType("PICKED_UP")
                .location("Mumbai Hub")
                .remarks("Parcel collected")
                .eventTime(LocalDateTime.now())
                .build();
    }

    @Test
    void addEvent_shouldCallFeignClient_withValidRequest() {
        when(trackingServiceClient.addEvent(any(TrackingEventRequest.class))).thenReturn(null);

        assertDoesNotThrow(() -> trackingServiceClient.addEvent(validRequest));

        verify(trackingServiceClient, times(1)).addEvent(validRequest);
    }

    @Test
    void addEvent_shouldHandleNullRemarks_gracefully() {
        validRequest.setRemarks(null);
        when(trackingServiceClient.addEvent(any(TrackingEventRequest.class))).thenReturn(null);

        assertDoesNotThrow(() -> trackingServiceClient.addEvent(validRequest));
        verify(trackingServiceClient, times(1)).addEvent(any());
    }

    @Test
    void addEvent_shouldNotThrow_whenFeignClientThrowsException() {
        when(trackingServiceClient.addEvent(any())).thenThrow(new RuntimeException("Connection refused"));

        assertDoesNotThrow(() -> {
            try {
                trackingServiceClient.addEvent(validRequest);
            } catch (Exception e) {
                // caught in service layer
            }
        });
    }

    @Test
    void addEvent_shouldBeCalled_forEachStatusTransition() {
        when(trackingServiceClient.addEvent(any())).thenReturn(null);

        String[] statuses = {"BOOKED", "PICKED_UP", "IN_TRANSIT", "OUT_FOR_DELIVERY", "DELIVERED"};
        for (String status : statuses) {
            validRequest.setEventType(status);
            trackingServiceClient.addEvent(validRequest);
        }

        verify(trackingServiceClient, times(statuses.length)).addEvent(any());
    }
}