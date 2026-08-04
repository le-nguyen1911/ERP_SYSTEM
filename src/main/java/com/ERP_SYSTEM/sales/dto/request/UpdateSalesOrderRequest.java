package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UpdateSalesOrderRequest(

        @NotNull(message = "Ngày giao hàng không được để trống")
        @Future(message = "Ngày giao hàng phải sau ngày hiện tại")
        LocalDate deliveryDate,

        @DecimalMin(value = "0.0", message = "Thuế suất không được âm")
        @DecimalMax(value = "100.0", message = "Thuế suất không được vượt quá 100%")
        BigDecimal taxPercentage,

        @DecimalMin(value = "0.0", message = "Phí vận chuyển không được âm")
        BigDecimal shippingCost,

        @DecimalMin(value = "0.0", message = "Số tiền giảm giá không được âm")
        BigDecimal discountAmount,

        @Size(max = 100)
        String paymentTerms,

        String shippingAddress,

        String notes
) {
}