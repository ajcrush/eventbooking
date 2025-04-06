package com.eventbooking.repository;

import com.eventbooking.model.Role;
import com.eventbooking.model.Role.RoleName;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(RoleName name);
}
