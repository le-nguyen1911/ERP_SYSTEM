package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateUnitRequest(
        @NotBlank(message = "Tên đơn vị không được để trống")
        @Size(max = 50, message = "Tối đa 50 ký tự")
        String name,

        @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
        String description
) {
}
