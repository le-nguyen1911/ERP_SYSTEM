package com.ERP_SYSTEM.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum SupplierStatus {
    ACTIVE("Hoạt động", "Nhà cung cấp đang hoạt động, có thể đặt hàng"),
    INACTIVE("Không hoạt động", "Nhà cung cấp bị ngừng, không thể đặt hàng mới");

    private final String displayName;
    private final String description;
}
