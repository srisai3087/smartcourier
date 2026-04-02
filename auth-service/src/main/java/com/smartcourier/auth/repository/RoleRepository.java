package com.smartcourier.auth.repository;

import com.smartcourier.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {

    // SELECT * FROM roles WHERE name = ? LIMIT 1
    Optional<Role> findByName(String name);

}
