package com.ERP_SYSTEM.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseOrderItemStatus {
    PENDING("Chờ nhận", "Chưa nhận hàng"),
    PARTIALLY_RECEIVED("Nhận một phần", "Nhận được một số lượng, còn chờ"),
    FULLY_RECEIVED("Nhận đủ", "Nhận đủ số lượng đặt hàng"),
    CANCELLED("Đã hủy", "Item bị hủy");


    private final String displayName;
    private final String description;

}
