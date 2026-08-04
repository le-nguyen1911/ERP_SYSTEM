package com.ERP_SYSTEM.sales.dto.request;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SalesOrderSearchRequest(
        UUID customerId,
        SalesOrderStatus status,
        LocalDateTime fromDate,
        LocalDateTime toDate
) {
}
