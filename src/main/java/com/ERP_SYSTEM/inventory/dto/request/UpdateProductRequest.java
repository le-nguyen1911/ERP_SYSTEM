package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public record UpdateProductRequest(
        String name,
        String description,

        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Giá phải lớn hơn 0"
        )
        BigDecimal price,

        Long categoryId,
        Long unitId,
        Boolean active
) {
}
