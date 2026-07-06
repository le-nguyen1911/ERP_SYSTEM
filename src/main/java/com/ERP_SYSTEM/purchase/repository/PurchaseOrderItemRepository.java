package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.PurchaseOrderItem;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderItemRepository extends JpaRepository<PurchaseOrderItem, UUID> {
    Optional<PurchaseOrderItem> findByIdAndIsDeletedFalse(UUID id);

    List<PurchaseOrderItem> findByPurchaseOrderIdAndIsDeletedFalse(UUID purchaseOrderId);

    List<PurchaseOrderItem> findByProductIdAndIsDeletedFalse(UUID productId);

    List<PurchaseOrderItem> findByPurchaseOrderIdAndStatusAndIsDeletedFalse(
            UUID purchaseOrderId, PurchaseOrderItemStatus status);
}
