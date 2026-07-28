package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.DeliveryItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface DeliveryItemRepository extends JpaRepository<DeliveryItem, UUID> {

    List<DeliveryItem> findByDeliveryIdAndIsDeletedFalse(UUID deliveryId);

    List<DeliveryItem> findBySalesOrderItemIdAndIsDeletedFalse(UUID salesOrderItemId);

    @Query("""
            SELECT COALESCE(SUM(di.quantityDelivered), 0)
            FROM DeliveryItem di
            WHERE di.salesOrderItem.id = :salesOrderItemId
            AND di.isDeleted = false
            AND di.delivery.status <> 'CANCELLED'
            """)
    BigDecimal sumActiveClaimedQuantity(@Param("salesOrderItemId") UUID salesOrderItemId);
}