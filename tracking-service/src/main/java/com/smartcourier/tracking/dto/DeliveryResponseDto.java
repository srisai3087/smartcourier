package com.smartcourier.tracking.dto;

import lombok.Data;

@Data
public class DeliveryResponseDto {

    private Long id;
    private String trackingNumber;
    private String status;
}