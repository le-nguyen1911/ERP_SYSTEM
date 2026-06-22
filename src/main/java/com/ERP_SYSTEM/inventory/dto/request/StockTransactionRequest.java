package com.ERP_SYSTEM.inventory.dto.request;

import com.ERP_SYSTEM.inventory.entity.StockTransaction.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record StockTransactionRequest(
        @NotNull(message = "Mã sản phẩm không đuược bỏ trống")
        @Positive(message = "Mã sản phẩm không hợp lệ")
        UUID productId,
        @NotNull(message = "Mã kho không đuược bỏ trống")
        @Positive(message = "Mã kho không hợp lệ")
        UUID warehouseId,

        @NotNull(message = "Loại giao dịch không được để trống")
        TransactionType type,

        @NotNull(message = "Số lượng không được để trống")
        @Min(value = 1, message = "Số lượng tối thiểu là 1")
        @Max(value = 1_000_000,
                message = "Số lượng tối đa 1,000,000 mỗi giao dịch")
        Integer quantity,

        @DecimalMin(
                value = "0.0",
                inclusive = true,
                message = "Đơn giá không được âm"
        )
        @Digits(integer = 13, fraction = 2,
                message = "Đơn giá tối đa 13 số nguyên và 2 số thập phân")
        BigDecimal unitPrice,

        @Size(max = 500, message = "Ghi chú tối đa 500 ký tự")
        String note
) {
}
