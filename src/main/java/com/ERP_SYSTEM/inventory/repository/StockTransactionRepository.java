package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface StockTransactionRepository extends JpaRepository<StockTransaction, UUID> {
    Page<StockTransaction> findByProductId(UUID productId, Pageable pageable);

    Page<StockTransaction> findByWarehouseId(UUID warehouseId, Pageable pageable);

    List<StockTransaction> findByCreatedAtBetween(LocalDateTime createdAt, LocalDateTime updatedAt);
}
