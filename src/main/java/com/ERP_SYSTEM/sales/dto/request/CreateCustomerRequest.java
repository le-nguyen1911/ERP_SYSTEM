package com.ERP_SYSTEM.sales.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateCustomerRequest(
        @NotBlank(message = "Mã khách hàng không được để trống")
        @Size(max = 50)
        String customerCode,

        @NotBlank(message = "Tên khách hàng không được để trống")
        @Size(max = 255)
        String customerName,

        @NotBlank(message = "Người liên hệ không được để trống")
        @Size(max = 100)
        String contactPerson,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100)
        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(min = 8, max = 20)
        String phone,

        @NotBlank(message = "Địa chỉ không được để trống")
        String address,

        @Size(max = 100)
        String city,

        @Size(max = 100)
        String country,

        @Size(max = 50)
        String taxId,

        @Size(max = 50)
        String paymentTerms
) {
}
