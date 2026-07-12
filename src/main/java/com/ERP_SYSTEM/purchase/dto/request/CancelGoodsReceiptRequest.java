package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelGoodsReceiptRequest(

        @NotBlank(message = "Lý do hủy không được để trống")
        String reason
) {
}
