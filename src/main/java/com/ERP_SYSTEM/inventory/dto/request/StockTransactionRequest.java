package com.ERP_SYSTEM.inventory.dto.request;

import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record StockTransactionRequest(
        @NotNull(message = "Sản phẩm không được để trống")
        Long productId,

        @NotNull(message = "Kho không được để trống")
        Long warehouseId,

        @NotNull(message = "Loại giao dịch không được để trống")
        StockTransaction.TransactionType type,
       

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        Integer quantity,

        @DecimalMin(
                value = "0.0",
                inclusive = false,
                message = "Đơn giá phải lớn hơn 0"
        )
        BigDecimal unitPrice,

        String note
) {
}
