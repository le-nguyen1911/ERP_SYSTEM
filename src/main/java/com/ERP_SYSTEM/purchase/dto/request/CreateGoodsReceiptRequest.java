package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public record CreateGoodsReceiptRequest(
        @NotNull(message = "Đơn đặt hàng không được để trống")
        UUID purchaseOrderId,

        @NotEmpty(message = "Phiếu nhận hàng phải có ít nhất 1 dòng sản phẩm")
        @Valid
        List<GoodsReceiptItemRequest> items
) {
}
