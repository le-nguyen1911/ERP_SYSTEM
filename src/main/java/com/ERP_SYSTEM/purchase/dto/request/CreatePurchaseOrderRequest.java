package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record CreatePurchaseOrderRequest(
        @NotNull(message = "Nhà cung cấp không được để trống")
        UUID supplierId,

        @NotNull(message = "Kho nhận hàng không được để trống")
        UUID warehouseId,

        UUID requisitionId,

        @NotNull(message = "Ngày giao hàng không được để trống")
        @Future(message = "Ngày giao hàng phải sau ngày hiện tại")
        LocalDate deliveryDate,

        @NotBlank(message = "Đơn vị tiền tệ không được để trống")
        @Size(min = 3, max = 3, message = "Mã tiền tệ phải đúng 3 ký tự (VD: VND, USD)")
        String currency,

        @DecimalMin(value = "0.0", inclusive = true, message = "Thuế suất không được âm")
        @DecimalMax(value = "100.0", message = "Thuế suất không được vượt quá 100%")
        java.math.BigDecimal taxPercentage,

        @DecimalMin(value = "0.0", inclusive = true, message = "Phí vận chuyển không được âm")
        java.math.BigDecimal shippingCost,

        @DecimalMin(value = "0.0", inclusive = true, message = "Số tiền giảm giá không được âm")
        java.math.BigDecimal discountAmount,

        @Size(max = 100)
        String paymentTerms,

        @Size(max = 50)
        String incoterms,

        String notes,

        @NotEmpty(message = "Đơn hàng phải có ít nhất 1 sản phẩm")
        @Valid
        List<PurchaseOrderItemRequest> items
) {
}
