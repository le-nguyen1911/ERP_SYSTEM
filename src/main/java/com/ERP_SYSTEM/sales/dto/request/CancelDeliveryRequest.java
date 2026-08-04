package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CancelDeliveryRequest(
        @NotBlank(message = "Lý do hủy không được để trống")
        String reason
) {
}