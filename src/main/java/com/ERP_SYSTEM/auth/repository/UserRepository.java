package com.ERP_SYSTEM.auth.repository;

import com.ERP_SYSTEM.auth.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    @Query("""
            SELECT DISTINCT u FROM User u
            JOIN u.roles r
            JOIN r.permissions p
            WHERE p.name = :permissionName
            AND u.enabled = true
            """)
    List<User> findByPermissionName(@Param("permissionName") String permissionName);

    /**
     * Returns a Page of users whose roles do NOT include SUPER_ADMIN.
     * Used when an ADMIN queries the user list.
     */
    @Query(value = """
            SELECT DISTINCT u FROM User u
            WHERE NOT EXISTS (
                SELECT 1 FROM Role r
                JOIN r.users ru
                WHERE ru = u AND UPPER(r.name) = 'SUPER_ADMIN'
            )
            """,
           countQuery = """
            SELECT COUNT(DISTINCT u) FROM User u
            WHERE NOT EXISTS (
                SELECT 1 FROM Role r
                JOIN r.users ru
                WHERE ru = u AND UPPER(r.name) = 'SUPER_ADMIN'
            )
            """)
    Page<User> findAllExcludingSuperAdmin(Pageable pageable);

    /** Count users that hold the SUPER_ADMIN role (for last-admin protection). */
    @Query("""
            SELECT COUNT(DISTINCT u) FROM User u
            JOIN u.roles r
            WHERE UPPER(r.name) = 'SUPER_ADMIN'
            """)
    long countSuperAdminUsers();
}
