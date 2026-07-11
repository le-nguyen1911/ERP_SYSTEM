package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.GoodsReceipt;
import com.ERP_SYSTEM.purchase.enums.GoodsReceiptStatus;
import com.ERP_SYSTEM.purchase.enums.InventoryImportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface GoodsReceiptRepository extends JpaRepository<GoodsReceipt, UUID> {
    Optional<GoodsReceipt> findByIdAndIsDeletedFalse(UUID id);

    Optional<GoodsReceipt> findByGrNumberAndIsDeletedFalse(String grNumber);

    boolean existsByGrNumberAndIsDeletedFalse(String grNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT gr FROM GoodsReceipt gr WHERE gr.id = :id AND gr.isDeleted = false")
    Optional<GoodsReceipt> findByIdForUpdate(@Param("id") UUID id);

    Page<GoodsReceipt> findByPurchaseOrderIdAndIsDeletedFalse(UUID purchaseOrderId, Pageable pageable);

    Page<GoodsReceipt> findByIsDeletedFalse(Pageable pageable);

    Page<GoodsReceipt> findBySupplierIdAndIsDeletedFalse(UUID supplierId, Pageable pageable);

    Page<GoodsReceipt> findByStatusAndIsDeletedFalse(GoodsReceiptStatus status, Pageable pageable);

    Page<GoodsReceipt> findByWarehouseIdAndIsDeletedFalse(UUID warehouseId, Pageable pageable);

    List<GoodsReceipt> findByInventoryImportStatus(InventoryImportStatus inventoryImportStatus);
}
