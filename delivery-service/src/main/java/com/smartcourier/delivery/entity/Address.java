package com.smartcourier.delivery.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "addresses")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Address {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fullName;      // Name of person at this address

    @Column(nullable = false)
    private String street;        // Street line 1

    private String street2;       // Apartment/suite (optional)

    @Column(nullable = false)
    private String city;

    @Column(nullable = false)
    private String state;

    @Column(nullable = false)
    private String pinCode;       // PIN/ZIP code for routing

    @Column(nullable = false)
    private String country;

    @Column(nullable = false)
    private String phone;         // Contact number at this address
}
