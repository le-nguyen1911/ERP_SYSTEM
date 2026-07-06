package com.ERP_SYSTEM.purchase.repository;

import com.ERP_SYSTEM.purchase.entity.PurchaseRequisition;
import com.ERP_SYSTEM.purchase.enums.RequisitionStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface PurchaseRequisitionRepository extends JpaRepository<PurchaseRequisition, UUID> {
    Optional<PurchaseRequisition> findByIdAndIsDeletedFalse(UUID id);

    Optional<PurchaseRequisition> findByPrNumberAndIsDeletedFalse(String prNumber);

    boolean existsByPrNumberAndIsDeletedFalse(String prNumber);

    Page<PurchaseRequisition> findByStatusAndIsDeletedFalse(RequisitionStatus status, Pageable pageable);

    Page<PurchaseRequisition> findByRequestedByIdAndIsDeletedFalse(UUID requestedById, Pageable pageable);
}
