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
