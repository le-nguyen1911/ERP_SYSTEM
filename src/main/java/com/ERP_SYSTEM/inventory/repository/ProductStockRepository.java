package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.ProductStock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductStockRepository extends JpaRepository<ProductStock, UUID> {

    Optional<ProductStock> findByProductIdAndWarehouseId(UUID productId, UUID warehouseId);

    List<ProductStock> findByProductId(UUID productId);

    List<ProductStock> findByWarehouseId(UUID warehouseId);

    boolean existsByWarehouseIdAndQuantityGreaterThan(
            UUID warehouseId,
            Integer quantity
    );

    @Query("""
                        SELECT ps FROM ProductStock ps
                        WHERE ps.quantity <= ps.minQuantity
                        AND ps.minQuantity > 0
            """
    )
    List<ProductStock> findLowStockProducts();
}
