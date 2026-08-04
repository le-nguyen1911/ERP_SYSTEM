package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateDeliveryRequest(
        @NotNull(message = "Đơn bán hàng không được để trống")
        UUID salesOrderId,

        @NotEmpty(message = "Phiếu giao hàng phải có ít nhất 1 dòng sản phẩm")
        @Valid
        List<DeliveryItemRequest> items
) {
}
