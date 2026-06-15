package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record CreateProductRequest(
        @NotBlank(message = "Mã sản phẩm không được để trống")
        String code,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        String name,

        String description,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Giá không được nhỏ hơn không"
        )
        BigDecimal price,

        @NotNull(message = "Danh mục không được bỏ trống")
        Long categoryId,

        @NotNull(message = "Đơn vị không được bỏ trống")
        Long unitId

) {
}
