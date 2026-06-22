package com.ERP_SYSTEM.inventory.dto.response;

import com.ERP_SYSTEM.inventory.entity.StockTransaction.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record StockTransactionResponse(
        UUID id,
        ProductResponse product,
        WarehouseResponse warehouse,
        TransactionType type,
        Integer quantity,
        BigDecimal unitPrice,
        String note,
        String createdBy,
        LocalDateTime createdAt


) {
}
