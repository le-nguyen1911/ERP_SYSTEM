package com.ERP_SYSTEM.inventory.event;

import java.util.UUID;

public record LowStockDetectedEvent(
        UUID productId,
        String productName,
        UUID warehouseId,
        Integer currentQuantity,
        Integer minQuantity
) {
}
