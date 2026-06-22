package com.ERP_SYSTEM.inventory.dto.response;

import java.math.BigDecimal;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String code,
        String name,
        String description,
        BigDecimal price,
        Boolean active,
        CategoryResponse category,
        UnitResponse unit

) {
}
