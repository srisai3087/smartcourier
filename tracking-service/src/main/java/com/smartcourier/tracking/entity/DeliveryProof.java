package com.smartcourier.tracking.entity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery_proofs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class DeliveryProof {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private Long deliveryId;
    private String recipientName;     // Name of person who received the parcel
    private String signatureImagePath;// Path to recipient's signature image
    private String photoPath;         // Path to delivery confirmation photo
    private String remarks;           // Agent notes
    @Column(nullable = false)
    private LocalDateTime deliveredAt;
    @Column(nullable = false)
    private Long agentId;             // userId of delivery agent
}
