package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import com.ERP_SYSTEM.sales.entity.SalesOrder;
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
public interface SalesOrderRepository extends JpaRepository<SalesOrder, UUID> {
    Optional<SalesOrder> findByIdAndIsDeletedFalse(UUID id);

    Optional<SalesOrder> findBySoNumberAndIsDeletedFalse(String number);

    boolean existsBySoNumberAndIsDeletedFalse(String number);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT so FROM SalesOrder so WHERE so.id = :id AND so.isDeleted = false")
    Optional<SalesOrder> findByIdForUpdate(@Param("id") UUID id);

    Page<SalesOrder> findByIsDeletedFalse(Pageable pageable);

    Page<SalesOrder> findByCustomerIdAndIsDeletedFalse(UUID customerId, Pageable pageable);

    Page<SalesOrder> findByStatusAndIsDeletedFalse(SalesOrderStatus status, Pageable pageable);

    Page<SalesOrder> findByCustomerIdAndStatusAndIsDeletedFalse(
            UUID customerId, SalesOrderStatus status, Pageable pageable);

    @Query("""
            SELECT so FROM SalesOrder so
            WHERE so.isDeleted = false
            AND (:customerId IS NULL OR so.customer.id = :customerId)
            AND (:status IS NULL OR so.status = :status)
            AND (:fromDate IS NULL OR so.soDate >= :fromDate)
            AND (:toDate IS NULL OR so.soDate <= :toDate)
            """)
    Page<SalesOrder> searchSalesOrders(
            @Param("customerId") UUID customerId,
            @Param("status") SalesOrderStatus status,
            @Param("fromDate") LocalDateTime fromDate,
            @Param("toDate") LocalDateTime toDate,
            Pageable pageable);
}
