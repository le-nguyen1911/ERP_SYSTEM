package com.ERP_SYSTEM.purchase.dto.request;

import com.ERP_SYSTEM.purchase.enums.SupplierStatus;
import jakarta.validation.constraints.*;

public record UpdateSupplierRequest(
        @NotBlank(message = "Tên nhà cung cấp không được để trống")
        @Size(max = 255)
        String supplierName,

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
        String bankName,
        String bankAccountNo,
        String bankAccountHolder,
        String paymentTerms,

        @Pattern(regexp = "^(A\\+|A|B|C|D)$")
        String rating,

        String taxId,

        @NotNull(message = "Trạng thái không được để trống")
        SupplierStatus status
) {
}
