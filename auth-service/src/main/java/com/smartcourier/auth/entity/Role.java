package com.smartcourier.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Role Entity - Represents user roles in the system.
 *
 * Roles stored in the 'roles' table in auth_db.
 * Pre-populated via data.sql or Flyway migration.
 *
 * Expected values: ROLE_CUSTOMER, ROLE_ADMIN
 *
 * JPA Annotation Notes:
 *  @Entity  - marks this class as a JPA-managed entity (maps to DB table)
 *  @Table   - specifies the table name in auth_db
 */
@Entity
@Table(name = "roles")
@Data               // Lombok: generates getters, setters, equals, hashCode, toString
@NoArgsConstructor  // Lombok: generates no-arg constructor (required by JPA)
@AllArgsConstructor // Lombok: generates all-args constructor
public class Role {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT in MySQL
    private Long id;

    // Role name: ROLE_CUSTOMER or ROLE_ADMIN
    // Unique constraint ensures no duplicate role names
    @Column(unique = true, nullable = false, length = 50)
    private String name;
}
