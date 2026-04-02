package com.smartcourier.admin.entity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "hubs")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Hub {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String hubName;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String state;
    @Column(nullable = false)
    private String pinCode;
    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;    // Soft-disable without deleting
}
