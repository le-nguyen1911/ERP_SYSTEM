package com.ERP_SYSTEM.inventory.dto.response;

import java.util.UUID;

public record WarehouseResponse(
        UUID id,
        String name,
        String location,
        String description,
        Boolean active
) {
}
