package com.smartcourier.admin.dto;



import lombok.Data;

@Data
public class ExceptionCreateRequest {
    private Long deliveryId;
    private String exceptionType;
    private String reason;
}
