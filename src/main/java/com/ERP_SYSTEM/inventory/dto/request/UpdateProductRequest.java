package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record UpdateProductRequest(


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
        UUID unitId,

        Boolean active

) {
}
