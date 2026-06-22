package com.ERP_SYSTEM.inventory.dto.response;

import java.util.UUID;

public record UnitResponse(
        UUID id,
        String name,
        String description
) {
}
