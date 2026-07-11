package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Repository
public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, UUID> {
    List<GoodsReceiptItem> findByGoodsReceiptIdAndIsDeletedFalse(UUID goodsReceiptId);

    List<GoodsReceiptItem> findByPurchaseOrderItemIdAndIsDeletedFalse(UUID purchaseOrderItemId);

    @Query("""
            SELECT COALESCE(SUM(gri.quantityAccepted + gri.quantityRejected), 0)
            FROM GoodsReceiptItem gri
            WHERE gri.purchaseOrderItem.id = :purchaseOrderItemId
            AND gri.isDeleted = false
            AND gri.goodsReceipt.status NOT IN ('CANCELLED', 'QC_FAILED')
            """)
    BigDecimal sumActiveClaimedQuantity(@Param("purchaseOrderItemId") UUID purchaseOrderItemId);
}
