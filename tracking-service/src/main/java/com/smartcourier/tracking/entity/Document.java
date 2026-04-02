package com.smartcourier.tracking.entity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Document {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private Long deliveryId;
    @Column(nullable = false)
    private String documentType;   // INVOICE | LABEL | CUSTOMS | OTHER
    @Column(nullable = false)
    private String fileName;       // Original filename for display
    @Column(nullable = false)
    private String storagePath;    // Server filesystem path or cloud key
    private Long fileSizeBytes;
    @CreationTimestamp
    private LocalDateTime uploadedAt;
}
