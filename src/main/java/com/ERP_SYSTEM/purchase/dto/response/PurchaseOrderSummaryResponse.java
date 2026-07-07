package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PurchaseOrderSummaryResponse(
        UUID id,
        String poNumber,
        UUID supplierId,
        String supplierName,
        LocalDateTime poDate,
        LocalDate deliveryDate,
        String currency,
        BigDecimal grandTotal,
        PurchaseOrderStatus status,
        LocalDateTime createdAt
) {
}
