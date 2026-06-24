package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record UpdateMinQuantityRequest(

        @NotNull(message = "Sản phẩm không được để trống")
        UUID productId,

        @NotNull(message = "Kho không được để trống")
        UUID warehouseId,

        @NotNull(message = "Mức tối thiểu không được để trống")
        @Min(value = 0, message = "Mức tối thiểu không được âm")
        Integer minQuantity
) {
}
