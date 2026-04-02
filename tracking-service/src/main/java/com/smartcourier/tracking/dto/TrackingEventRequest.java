package com.smartcourier.tracking.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TrackingEventRequest {

    @NotNull
    private Long deliveryId;

    @NotBlank
    private String trackingNumber;

    @NotBlank
    private String eventType;

    private String location;
    private String remarks;

    @NotNull
    private LocalDateTime eventTime;
}