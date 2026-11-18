package com.ERP_SYSTEM.auth.repository;

import com.ERP_SYSTEM.auth.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RoleRepository extends JpaRepository<Role, UUID> {
    Optional<Role> findByName(String name);

    /** Returns all roles except SUPER_ADMIN – used when ADMIN calls GET /roles */
    @Query("SELECT r FROM Role r WHERE UPPER(r.name) <> 'SUPER_ADMIN'")
    List<Role> findAllExcludingSuperAdmin();
}
