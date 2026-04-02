package com.smartcourier.admin.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;
@Entity
@Table(name = "exception_logs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class ExceptionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "delivery_id", nullable = false)
    private Long deliveryId;

    @Column(name = "exception_type", nullable = false)
    private String exceptionType;

    @Column(nullable = false)
    private String reason;

    @Column(name = "resolution")
    private String resolution;

    @Column(name = "resolved_at")
    private LocalDateTime resolvedAt;

    @Column(name = "resolved_by")
    private Long resolvedBy;

    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
}