package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record UpdateSalesOrderItemRequest(
        @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0.0001", message = "Số lượng phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal quantity,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0.0001", message = "Giá phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal unitPrice,


        String description
) {
}
