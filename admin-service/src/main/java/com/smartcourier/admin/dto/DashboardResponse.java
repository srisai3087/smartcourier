package com.smartcourier.admin.dto;
import lombok.Builder; import lombok.Data;

@Data @Builder
public class DashboardResponse {
    private long totalDeliveries;
    private long totalDelivered;
    private long totalPending;     // BOOKED + PICKED_UP + IN_TRANSIT + OUT_FOR_DELIVERY
    private long totalFailed;
    private long totalDelayed;
    private long unresolvedExceptions;
    private long totalHubs;
}
