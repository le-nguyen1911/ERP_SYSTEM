package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.GoodsReceiptItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface GoodsReceiptItemRepository extends JpaRepository<GoodsReceiptItem, UUID> {
    List<GoodsReceiptItem> findByGoodsReceiptIdAndIsDeletedFalse(UUID goodsReceiptId);

    List<GoodsReceiptItem> findByPurchaseOrderItemIdAndIsDeletedFalse(UUID purchaseOrderItemId);
}
