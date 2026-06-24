package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UpdateWarehouseRequest(
        @NotBlank(message = "Tên kho không được để trống")
        @Size(max = 100, message = "Tối đa 100 ký tự")
        String name,
        String location,
        String description,
        boolean active
) {
}
