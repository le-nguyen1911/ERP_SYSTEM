package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.RequisitionStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseRequisitionResponse(
        UUID id,
        String prNumber,
        UUID requestedById,
        LocalDate requiredDate,
        String purpose,
        RequisitionStatus status,
        BigDecimal estimatedTotal,
        UUID approvedById,
        LocalDateTime approvalDate,
        String rejectionReason,          // ← MỚI
        UUID purchaseOrderId,
        LocalDateTime createdAt
) {
}
