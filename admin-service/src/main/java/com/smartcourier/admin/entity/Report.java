package com.smartcourier.admin.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "reports")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Report {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String reportType;          // DAILY | WEEKLY | MONTHLY
    private LocalDate reportDate;       // Date this report covers
    private Long totalDeliveries;
    private Long delivered;
    private Long failed;
    private Long pending;              // All non-terminal states combined
    @CreationTimestamp
    private LocalDateTime generatedAt;
}
