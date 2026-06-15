package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateUnitRequest(
        @NotBlank(message = "Tên đơn vị không được để trống")
        String name,
        String description
) {
}
