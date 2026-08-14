package com.ERP_SYSTEM.purchase.event;

import java.util.UUID;

public record PurchaseOrderApprovedEvent(
        UUID purchaseOrderId,
        String poNumber,
        UUID createdById,
        UUID approvedById) {
}
