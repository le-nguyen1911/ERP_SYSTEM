package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.PurchaseOrder;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseOrderRepository extends JpaRepository<PurchaseOrder, UUID> {
    Optional<PurchaseOrder> findByIdAndIsDeletedFalse(UUID id);

    Optional<PurchaseOrder> findByPoNumberAndIsDeletedFalse(String poNumber);

    boolean existsByPoNumberAndIsDeletedFalse(String poNumber);


    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT po FROM PurchaseOrder po WHERE po.id = :id AND po.isDeleted = false")
    Optional<PurchaseOrder> findByIdForUpdate(@Param("id") UUID id);

    Page<PurchaseOrder> findByIsDeletedFalse(Pageable pageable);

    Page<PurchaseOrder> findBySupplierIdAndIsDeletedFalse(UUID supplierId, Pageable pageable);

    Page<PurchaseOrder> findByStatusAndIsDeletedFalse(PurchaseOrderStatus status, Pageable pageable);

    Page<PurchaseOrder> findBySupplierIdAndStatusAndIsDeletedFalse(
            UUID supplierId, PurchaseOrderStatus status, Pageable pageable);

    @Query("""
            SELECT po FROM PurchaseOrder po
            WHERE po.isDeleted = false
            AND (:supplierId IS NULL OR po.supplier.id = :supplierId)
            AND (:status IS NULL OR po.status = :status)
            AND (cast(:fromDate as timestamp) IS NULL OR po.poDate >= :fromDate)
            AND (cast(:toDate as timestamp) IS NULL OR po.poDate <= :toDate)
            """)
    Page<PurchaseOrder> searchPurchaseOrders(
            @Param("supplierId") UUID supplierId,
            @Param("status") PurchaseOrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}
