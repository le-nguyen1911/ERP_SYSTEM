package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.Supplier;
import com.ERP_SYSTEM.purchase.enums.SupplierStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SupplierRepository extends JpaRepository<Supplier, UUID> {
    Optional<Supplier> findByIdAndIsDeletedFalse(UUID id);

    Optional<Supplier> findBySupplierCodeAndIsDeletedFalse(String supplierCode);

    boolean existsBySupplierCodeAndIsDeletedFalse(String supplierCode);

    Page<Supplier> findByIsDeletedFalse(Pageable pageable);

    Page<Supplier> findByStatusAndIsDeletedFalse(SupplierStatus status, Pageable pageable);

    @Query("""
            SELECT s FROM Supplier s
            WHERE s.isDeleted = false
            AND (:keyword IS NULL
                 OR LOWER(s.supplierName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(s.supplierCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR s.status = :status)
            """)
    Page<Supplier> searchSuppliers(
            @Param("keyword") String keyword,
            @Param("status") SupplierStatus status,
            Pageable pageable);
}
