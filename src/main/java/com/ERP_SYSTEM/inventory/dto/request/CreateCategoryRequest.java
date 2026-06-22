package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCategoryRequest(
        @NotBlank(message = "Tên danh mục không được bỏ trống")
        @Size(max = 100, message = "Tên danh mục tối đa 100 ký tự")
        String name,

        @Size(max = 255, message = "Mô tả tối đa 255 ký tự")
        String description

) {
}
