package com.smartcourier.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ProofResponse {

    private Long deliveryId;
    private String recipientName;
    private String signatureImagePath;
    private String photoPath;
    private String remarks;
    private LocalDateTime deliveredAt;
    private Long agentId;
}