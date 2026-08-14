package com.ERP_SYSTEM.sales.event;

import java.util.UUID;

public record SalesOrderApprovedEvent(
        UUID salesOrderId,
        String soNumber,
        UUID createdById,
        UUID approvedById
) {
}
