package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.SupplierStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record SupplierResponse(
        UUID id,
        String supplierCode,
        String supplierName,
        String contactPerson,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String bankName,
        String bankAccountNo,
        String bankAccountHolder,
        String paymentTerms,
        String rating,
        String taxId,
        SupplierStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
