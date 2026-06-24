package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.Warehouse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, UUID> {
    Optional<Warehouse> findByName(String name);

    boolean existsByName(String name);

    List<Warehouse> findByActiveTrue();


}
