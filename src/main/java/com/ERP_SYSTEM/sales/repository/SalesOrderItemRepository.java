package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderItemStatus;
import com.ERP_SYSTEM.sales.entity.SalesOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SalesOrderItemRepository extends JpaRepository<SalesOrderItem, UUID> {
    Optional<SalesOrderItem> findByIdAndIsDeletedFalse(UUID id);

    List<SalesOrderItem> findBySalesOrderIdAndIsDeletedFalse(UUID salesOrderId);

    List<SalesOrderItem> findByProductIdAndIsDeletedFalse(UUID productId);

    List<SalesOrderItem> findBySalesOrderIdAndStatusAndIsDeletedFalse(
            UUID salesOrderId, SalesOrderItemStatus status);
}
