package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record CreateProductRequest(

        @NotBlank(message = "Mã sản phẩm Không được bỏ trống")
        @Size(max = 50, message = "Tối đa 50 ký tự")
        String code,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 100, message = "Tối đa 100 ký tự")
        String name,

        String description,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Giá không được âm"
        )
        @Digits(
                integer = 13,
                fraction = 2,
                message = "Giá tối đa 13 số nguyên và 2 số thập phân"
        )
        BigDecimal price,

        @NotNull(message = "Danh mục không được để trống")
        UUID categoryId,
        @NotNull(message = "Đơn vị tính không được để trống")
        UUID unitId

) {
}
