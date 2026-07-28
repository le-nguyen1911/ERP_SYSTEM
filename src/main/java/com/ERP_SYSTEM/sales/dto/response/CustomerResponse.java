package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerResponse(
        UUID id,
        String customerCode,
        String customerName,
        String contactPerson,
        String email,
        String phone,
        String address,
        String city,
        String country,
        String taxId,
        String paymentTerms,
        CustomerStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
