package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Delivery;
import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
    Optional<Delivery> findByIdAndIsDeletedFalse(UUID id);

    Optional<Delivery> findByDeliveryNumberAndIsDeletedFalse(String deliveryNumber);

    boolean existsByDeliveryNumberAndIsDeletedFalse(String deliveryNumber);

    Page<Delivery> findByIsDeletedFalse(Pageable pageable);

    Page<Delivery> findBySalesOrderIdAndIsDeletedFalse(UUID salesOrderId, Pageable pageable);

    Page<Delivery> findByCustomerIdAndIsDeletedFalse(UUID customerId, Pageable pageable);

    Page<Delivery> findByStatusAndIsDeletedFalse(DeliveryStatus status, Pageable pageable);

    List<Delivery> findByInventoryExportStatus(InventoryExportStatus inventoryExportStatus);
}

