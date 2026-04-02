package com.smartcourier.delivery.repository;

import com.smartcourier.delivery.entity.Delivery;
import com.smartcourier.delivery.enums.DeliveryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, Long> {

    // SELECT * FROM deliveries WHERE customer_id = ? ORDER BY created_at DESC
    List<Delivery> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

    // SELECT * FROM deliveries WHERE tracking_number = ? LIMIT 1
    Optional<Delivery> findByTrackingNumber(String trackingNumber);

    // Paginated query for admin - all deliveries with optional filtering
    Page<Delivery> findAll(Pageable pageable);

    // Find all deliveries in a specific status (admin monitoring)
    List<Delivery> findByStatus(DeliveryStatus status);

    // Count deliveries per status (for admin dashboard KPIs)
    long countByStatus(DeliveryStatus status);

    // JPQL: count deliveries for a specific customer
    @Query("SELECT COUNT(d) FROM Delivery d WHERE d.customerId = :customerId")
    long countByCustomerId(Long customerId);
}
