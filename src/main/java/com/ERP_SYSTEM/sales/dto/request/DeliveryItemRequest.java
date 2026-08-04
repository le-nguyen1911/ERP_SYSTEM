package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.UUID;

public record DeliveryItemRequest(
        @NotNull(message = "Dòng đơn hàng (Sales Order Item) không được để trống")
        UUID salesOrderItemId,

        @NotNull(message = "Số lượng giao không được để trống")
        @DecimalMin(value = "0.0001", message = "Số lượng giao phải lớn hơn 0")
        BigDecimal quantityDelivered,

        @Size(max = 50)
        String batchNumber,

        String notes
) {
}
