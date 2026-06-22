package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateWarehouseRequest(
        @NotBlank(message = "Tên kho không được để trống")
        @Size(max = 100, message = "Tối đa 100 ký tự")
        String name,

        @Size(max = 255, message = "Tối đa 255 ký tự")
        String location,
        @Size(max = 255, message = "Tối đa 255 ký tự")
        String description
) {
}
