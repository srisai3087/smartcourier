package com.smartcourier.auth.repository;

import com.smartcourier.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * UserRepository - Spring Data JPA Repository for User entity.
 *
 * Spring Data JPA auto-generates SQL queries from method names.
 * No implementation class needed - Spring provides a proxy at runtime.
 *
 * Spring Module: Spring Data JPA
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    // SELECT * FROM users WHERE email = ? LIMIT 1
    Optional<User> findByEmail(String email);

    // SELECT COUNT(*) > 0 FROM users WHERE email = ?
    boolean existsByEmail(String email);
}
