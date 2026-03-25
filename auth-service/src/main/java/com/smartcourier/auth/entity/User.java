package com.smartcourier.auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

/**
 * User Entity - Core user record stored in auth_db.
 *
 * Maps to 'users' table. Linked to roles via a join table (user_roles).
 *
 * JPA Relationship Notes:
 *  @ManyToMany - A user can have multiple roles; a role belongs to many users
 *  FetchType.EAGER - Roles are loaded immediately with user (needed for security checks)
 *
 * Security Notes:
 *  - password field stores BCrypt-hashed value (never plain text)
 *  - email is used as the Spring Security username
 */
@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder // Lombok: enables builder pattern for clean object creation
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Customer's full name for display and notifications
    @Column(nullable = false)
    private String fullName;

    // Email is the unique login identifier (Spring Security username)
    @Column(unique = true, nullable = false)
    private String email;

    // BCrypt-hashed password - NEVER store plain text
    @Column(nullable = false)
    private String password;

    // Optional phone for SMS notifications (future enhancement)
    @Column
    private String phone;

    // Soft-enable/disable user without deleting record
    @Column(nullable = false)
    @Builder.Default
    private Boolean enabled = true;

    /**
     * Many-to-Many relationship with Role entity.
     * Creates a join table 'user_roles' (user_id, role_id).
     * EAGER fetch ensures roles are always available for security context.
     */
    @ManyToMany(fetch = FetchType.EAGER, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    @Builder.Default
    private Set<Role> roles = new HashSet<>();

    // Automatically set by Hibernate when the record is first persisted
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
