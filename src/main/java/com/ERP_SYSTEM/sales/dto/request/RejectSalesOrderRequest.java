package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectSalesOrderRequest(
        @NotBlank(message = "Lý do hủy đơn không được để trống")
        String reason
) {
}