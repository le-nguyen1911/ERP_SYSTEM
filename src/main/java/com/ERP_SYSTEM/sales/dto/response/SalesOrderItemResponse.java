package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderItemStatus;

import java.math.BigDecimal;
import java.util.UUID;

public record SalesOrderItemResponse(
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
        BigDecimal deliveredQuantity,
        SalesOrderItemStatus status,
        String notes
) {
}
