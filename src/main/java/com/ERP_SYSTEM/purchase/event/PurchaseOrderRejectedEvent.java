package com.ERP_SYSTEM.purchase.event;

import java.util.UUID;

public record PurchaseOrderRejectedEvent(
        UUID purchaseOrderId,
        String poNumber,
        UUID createdById,
        String rejectionReason
) {
}
