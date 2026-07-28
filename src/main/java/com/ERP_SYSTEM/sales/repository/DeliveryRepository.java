package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Delivery;
import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.awt.print.Pageable;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByIdAndIsDeletedFalse(UUID id);

    Optional<Delivery> findByDeliveryNumberAndIsDeletedFalse(String deliveryNumber);

    boolean existsByDeliveryNumberAndIsDeletedFalse(String deliveryNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT d FROM Delivery d WHERE d.id = :id AND d.isDeleted = false")
    Optional<Delivery> findByIdForUpdate(@Param("id") UUID id);

    Page<Delivery> findByIsDeletedFalse(Pageable pageable);

    Page<Delivery> findBySalesOrderIdAndIsDeletedFalse(UUID salesOrderId, Pageable pageable);

    Page<Delivery> findByCustomerIdAndIsDeletedFalse(UUID customerId, Pageable pageable);

    Page<Delivery> findByStatusAndIsDeletedFalse(DeliveryStatus status, Pageable pageable);

    List<Delivery> findByInventoryExportStatus(InventoryExportStatus inventoryExportStatus);
}

