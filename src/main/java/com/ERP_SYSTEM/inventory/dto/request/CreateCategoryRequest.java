package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CreateCategoryRequest(
        @NotBlank(message = "Tên danh mục không được để trống")
        String name,
        String description

) {
}
