package com.smartcourier.tracking.repository;
import com.smartcourier.tracking.entity.TrackingEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TrackingEventRepository extends JpaRepository<TrackingEvent, Long> {
    // Uses index on tracking_number - fast lookup even with millions of events
    List<TrackingEvent> findByTrackingNumberOrderByEventTimeDesc(String trackingNumber);
    List<TrackingEvent> findByDeliveryIdOrderByEventTimeDesc(Long deliveryId);
}
