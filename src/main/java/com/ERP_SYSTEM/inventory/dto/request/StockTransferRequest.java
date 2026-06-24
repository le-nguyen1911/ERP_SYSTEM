package com.ERP_SYSTEM.inventory.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record StockTransferRequest(

        @NotNull(message = "Sản phẩm không được để trống")
        UUID productId,

        @NotNull(message = "Kho nguồn không được để trống")
        UUID fromWarehouseId,

        @NotNull(message = "Kho đích không được để trống")
        UUID toWarehouseId,

        @NotNull(message = "Số lượng không được để trống")
        @Min(1)
        Integer quantity,

        @Size(max = 500)
        String note
) {
}
