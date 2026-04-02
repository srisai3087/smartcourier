package com.smartcourier.tracking.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrackingResponse {

    private String trackingNumber;
    private Long deliveryId;
    private List<EventDTO> events;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class EventDTO {
        private Long id;
        private String eventType;
        private String location;
        private String remarks;
        private LocalDateTime eventTime;
    }
}