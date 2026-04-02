package com.smartcourier.delivery.entity;

import com.smartcourier.delivery.enums.DeliveryStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "deliveries")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Delivery {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID-based tracking number generated at creation
    // Used by customers to track their parcel publicly
    @Column(unique = true, nullable = false)
    private String trackingNumber;

    // Customer who created this delivery (from auth_db via X-User-Id header)
    @Column(nullable = false)
    private Long customerId;

    // Current state in the delivery lifecycle state machine
    @Enumerated(EnumType.STRING)  // Store as readable string (e.g. "BOOKED") not integer
    @Column(nullable = false)
    private DeliveryStatus status;

    // DOMESTIC / EXPRESS / INTERNATIONAL
    @Column(nullable = false)
    private String serviceType;

    // Calculated charge based on weight × service rate
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal chargeAmount;

    // Customer-requested pickup time slot
    private LocalDateTime scheduledPickup;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "sender_address_id")
    private Address senderAddress;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "receiver_address_id")
    private Address receiverAddress;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "package_id")
    private Package parcelPackage;

    // Automatically managed by Hibernate - no manual setting needed
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;
}
