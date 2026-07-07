package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.PurchaseOrderItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemResponse(
        UUID id,
        UUID productId,
        String productCode,
        String productName,
        String productUnit,
        Integer lineNumber,
        BigDecimal quantity,
        BigDecimal unitPrice,
        BigDecimal lineTotal,
        String description,
        BigDecimal receivedQuantity,
        BigDecimal rejectedQuantity,
        PurchaseOrderItemStatus status,
        String notes
) {
}
