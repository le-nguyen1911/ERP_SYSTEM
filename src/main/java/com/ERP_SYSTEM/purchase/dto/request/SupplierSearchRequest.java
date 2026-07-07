package com.ERP_SYSTEM.purchase.dto.request;

import com.ERP_SYSTEM.purchase.enums.SupplierStatus;

public record SupplierSearchRequest(
        String keyword,
        SupplierStatus status
) {
}
