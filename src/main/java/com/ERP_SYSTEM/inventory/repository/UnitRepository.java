package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Optional<Unit> findByName(String name);

    boolean existsByName(String name);

    boolean existsProductById(UUID id);
}
