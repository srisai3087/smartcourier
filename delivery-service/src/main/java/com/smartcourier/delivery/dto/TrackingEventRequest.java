package com.smartcourier.delivery.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingEventRequest {
    private Long deliveryId;
    private String trackingNumber;
    private String eventType;
    private String location;
    private String remarks;
    private LocalDateTime eventTime;
}