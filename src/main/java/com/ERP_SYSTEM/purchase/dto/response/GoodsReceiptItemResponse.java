package com.ERP_SYSTEM.purchase.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoodsReceiptItemResponse(
        UUID id,
        UUID purchaseOrderItemId,
        UUID productId,
        String productName,
        BigDecimal quantityAccepted,
        BigDecimal quantityRejected,
        String batchNumber,
        LocalDate expiryDate,
        String notes
) {
}
