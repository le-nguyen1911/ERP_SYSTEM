package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateWarehouseRequest(
        @NotBlank(message = "Tên kho khoong được để trống")
        String name,
        String location,
        String description
) {
}
