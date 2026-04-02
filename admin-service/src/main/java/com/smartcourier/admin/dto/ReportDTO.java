package com.smartcourier.admin.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDTO {
    private Long id;
    private String reportType;
    private LocalDate reportDate;
    private Long totalDeliveries;
    private Long delivered;
    private Long failed;
    private Long pending;
    private LocalDateTime generatedAt;
}