package com.ERP_SYSTEM.purchase.event;

import java.util.UUID;

public record GoodsReceiptImportFailedEvent(
        UUID goodsReceiptId,
        String grNumber,
        UUID receivedById,
        String errorMessage
) {
}
