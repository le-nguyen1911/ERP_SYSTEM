package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record PurchaseOrderItemRequest(
        @NotNull(message = "Sản phẩm không được để trống")
        UUID productId,

        @NotBlank(message = "Mã sản phẩm không được để trống")
        @Size(max = 50)
        String productCode,

        @NotBlank(message = "Tên sản phẩm không được để trống")
        @Size(max = 255)
        String productName,

        @NotBlank(message = "Đơn vị tính không được để trống")
        @Size(max = 20)
        String productUnit,

        @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0.0001", message = "Số lượng phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4, message = "Số lượng tối đa 15 số nguyên, 4 số thập phân")
        BigDecimal quantity,

        @NotNull(message = "Đơn giá không được để trống")
        @DecimalMin(value = "0.0001", message = "Đơn giá phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4, message = "Đơn giá tối đa 15 số nguyên, 4 số thập phân")
        BigDecimal unitPrice,

        String description
) {
}
