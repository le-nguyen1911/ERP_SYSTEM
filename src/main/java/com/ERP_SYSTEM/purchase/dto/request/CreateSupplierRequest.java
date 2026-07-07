package com.ERP_SYSTEM.purchase.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateSupplierRequest(

        @NotBlank(message = "Mã nhà cung cấp không được để trống")
        @Size(max = 50, message = "Mã nhà cung cấp tối đa 50 ký tự")
        String supplierCode,

        @NotBlank(message = "Tên nhà cung cấp không được để trống")
        @Size(max = 255, message = "Tên nhà cung cấp tối đa 255 ký tự")
        String supplierName,

        @NotBlank(message = "Người liên hệ không được để trống")
        @Size(max = 100, message = "Tối đa 100 ký tự")
        String contactPerson,

        @NotBlank(message = "Email không được để trống")
        @Email(message = "Email không đúng định dạng")
        @Size(max = 100, message = "Tối đa 100 ký tự")
        String email,

        @NotBlank(message = "Số điện thoại không được để trống")
        @Size(min = 8, max = 20, message = "Số điện thoại phải từ 8 đến 20 ký tự")
        String phone,

        @NotBlank(message = "Địa chỉ không được để trống")
        String address,

        @Size(max = 100, message = "Tối đa 100 ký tự")
        String city,

        @Size(max = 100, message = "Tối đa 100 ký tự")
        String country,

        @Size(max = 100)
        String bankName,

        @Size(max = 50)
        String bankAccountNo,

        @Size(max = 100)
        String bankAccountHolder,

        @Size(max = 50)
        String paymentTerms,

        @Pattern(regexp = "^(A\\+|A|B|C|D)$", message = "Rating phải là A+, A, B, C hoặc D")
        String rating,

        @Size(max = 50)
        String taxId
) {
}
