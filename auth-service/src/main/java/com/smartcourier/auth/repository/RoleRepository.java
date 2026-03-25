package com.smartcourier.auth.repository;

import com.smartcourier.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * RoleRepository - Spring Data JPA Repository for Role entity.
 * <p>
 * Used during signup to fetch the appropriate role (ROLE_CUSTOMER)
 * and assign it to the new user.
 */
@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // SELECT * FROM roles WHERE name = ? LIMIT 1
    Optional<Role> findByName(String name);

}
