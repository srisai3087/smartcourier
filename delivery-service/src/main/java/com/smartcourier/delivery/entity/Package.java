package com.smartcourier.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "packages")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Actual weight in kilograms - primary billing metric
    @Column(nullable = false)
    private Double weightKg;

    // Dimensions for volumetric weight calculation: L*W*H / 5000
    private Double lengthCm;
    private Double widthCm;
    private Double heightCm;

    // Brief description of contents (for customs declarations)
    private String description;

    // Fragile parcels get special handling labels and care instructions
    @Column(nullable = false)
    @Builder.Default
    private Boolean isFragile = false;
}
