package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record GoodsReceiptItemRequest(
        @NotNull(message = "Dòng đơn hàng (PO Item) không được để trống")
        UUID purchaseOrderItemId,

        @NotNull(message = "Số lượng chấp nhận không được để trống")
        @DecimalMin(value = "0.0", inclusive = true, message = "Số lượng chấp nhận không được âm")
        BigDecimal quantityAccepted,

        @DecimalMin(value = "0.0", inclusive = true, message = "Số lượng từ chối không được âm")
        BigDecimal quantityRejected,

        @Size(max = 50)
        String batchNumber,

        @Future(message = "Hạn sử dụng phải sau ngày hiện tại")
        LocalDate expiryDate,

        String notes
) {
}
