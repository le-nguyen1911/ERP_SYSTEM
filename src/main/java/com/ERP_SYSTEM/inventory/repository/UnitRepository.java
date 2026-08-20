package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UnitRepository extends JpaRepository<Unit, UUID> {
    Optional<Unit> findByName(String name);

    boolean existsByName(String name);

    @Query("SELECT COUNT(p) > 0 FROM Product p WHERE p.unit.id = :id")
    boolean existsProductById(@Param("id") UUID id);
}
