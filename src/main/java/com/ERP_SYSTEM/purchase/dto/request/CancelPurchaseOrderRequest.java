package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelPurchaseOrderRequest(
        @NotBlank(message = "Lý do huỷ không được để trống")
        String reason
) {
}
