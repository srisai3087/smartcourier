package com.smartcourier.delivery.dto;

import com.smartcourier.delivery.enums.DeliveryStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryResponse {

    private Long id;
    private String trackingNumber;
    private Long customerId;
    private DeliveryStatus status;
    private String serviceType;
    private BigDecimal chargeAmount;
    private LocalDateTime scheduledPickup;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Flattened address fields for frontend convenience
    private String senderCity;
    private String senderState;
    private String receiverCity;
    private String receiverState;
    private String receiverName;

    // Package summary
    private Double weightKg;
    private Boolean isFragile;
}
