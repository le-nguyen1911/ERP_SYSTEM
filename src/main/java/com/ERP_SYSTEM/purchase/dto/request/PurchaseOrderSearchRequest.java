package com.ERP_SYSTEM.purchase.dto.request;

import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;

import java.time.LocalDate;
import java.util.UUID;

public record PurchaseOrderSearchRequest(
        UUID supplierId,
        PurchaseOrderStatus status,
        LocalDate fromDate,
        LocalDate toDate
) {
}
