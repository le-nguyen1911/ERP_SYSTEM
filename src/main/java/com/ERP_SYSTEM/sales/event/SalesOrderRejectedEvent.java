package com.ERP_SYSTEM.sales.event;

import java.util.UUID;

public record SalesOrderRejectedEvent(
        UUID salesOrderId,
        String soNumber,
        UUID createdById,
        String rejectionReason
) {
}
