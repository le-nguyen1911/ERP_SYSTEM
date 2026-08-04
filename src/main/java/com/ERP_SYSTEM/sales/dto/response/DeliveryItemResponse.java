package com.ERP_SYSTEM.sales.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryItemResponse(
        UUID id,
        UUID salesOrderItemId,
        UUID productId,
        String productName,
        BigDecimal quantityDelivered,
        String batchNumber,
        String notes
) {
}