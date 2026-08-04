package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record SalesOrderSummaryResponse(
        UUID id,
        String soNumber,
        UUID customerId,
        String customerName,
        LocalDateTime soDate,
        LocalDate deliveryDate,
        String currency,
        BigDecimal grandTotal,
        SalesOrderStatus status,
        LocalDateTime createdAt
) {
}
