package com.smartcourier.admin.dto;



import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ExceptionLogDTO {

    private Long id;
    private Long deliveryId;
    private String exceptionType;
    private String reason;
    private String resolution;
    private LocalDateTime createdAt;
    private LocalDateTime resolvedAt;
    private Long resolvedBy;
}