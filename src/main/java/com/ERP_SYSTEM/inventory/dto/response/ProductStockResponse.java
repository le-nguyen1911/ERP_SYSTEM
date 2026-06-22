package com.ERP_SYSTEM.inventory.dto.response;

import java.util.UUID;

public record ProductStockResponse(
        UUID id,
        ProductResponse product,
        WarehouseResponse warehouse,
        Integer quantity,
        Integer minQuantity,
        Boolean lowStock
) {
}
