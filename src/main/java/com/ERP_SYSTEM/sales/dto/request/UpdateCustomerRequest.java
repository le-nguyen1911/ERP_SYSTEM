package com.ERP_SYSTEM.sales.dto.request;

import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record UpdateCustomerRequest(
        @NotBlank(message = "Tên khách hàng không được để trống")
        @Size(max = 255)
        String customerName,

        @NotBlank(message = "Người liên hệ không được để trống")
        @Size(max = 100)
        String contactPerson,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(min = 8, max = 20)
        String phone,

        @NotBlank(message = "Địa chỉ không được để trống")
        String address,

        String city,
        String country,
        String taxId,
        String paymentTerms,

        @NotNull(message = "Trạng thái không được để trống")
        CustomerStatus status) {
}
