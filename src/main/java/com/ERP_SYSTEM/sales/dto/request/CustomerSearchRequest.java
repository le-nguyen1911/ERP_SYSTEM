package com.ERP_SYSTEM.sales.dto.request;

import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;

public record CustomerSearchRequest(
        String keyword,
        CustomerStatus status
) {
}
