package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

public record AddSalesOrderItemRequest(
        @NotNull(message = "Sản phẩm khong được bỏ trống")
        UUID productId,

        @NotBlank(message = "Mã sản phẩm không được bỏ trống")
        @Size(max = 50, message = "Tối đa 50 ký tự")
        String productCode,

        @NotBlank(message = "tên sản phẩm không được bỏ trống")
        @Size(max = 255, message = "Tối đa 50 ký tự")
        String productName,

        @NotBlank(message = "đơn vị tính không được bỏ trống")
        @Size(max = 255, message = "Tối đa 50 ký tự")
        String productUnit,

        @NotNull(message = "Số lượng không được để trống")
        @DecimalMin(value = "0.0001", message = "Số lượng phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal quantity,

        @NotNull(message = "Giá không được để trống")
        @DecimalMin(value = "0.0001", message = "Giá phải lớn hơn 0")
        @Digits(integer = 15, fraction = 4)
        BigDecimal unitPrice,


        String description
) {
}
