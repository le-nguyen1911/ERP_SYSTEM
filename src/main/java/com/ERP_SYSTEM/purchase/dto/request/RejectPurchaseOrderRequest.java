package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RejectPurchaseOrderRequest(
        @NotBlank(message = "Lý do từ chối không được để trống")
        String reason
) {
}
