package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdatePurchaseOrderItemRequest(
        @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0.0001", message = "Số lượng phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal quantity,

        @NotNull(message = "Đơn giá không được để trống")
        @DecimalMin(value = "0.0001", message = "Đơn giá phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal unitPrice,

        String description
) {
}
