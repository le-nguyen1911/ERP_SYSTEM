package com.ERP_SYSTEM.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PurchaseOrderStatus {
    DRAFT("Bản nháp", "Tạo mới, chưa gửi"),
    PENDING_APPROVAL("Chờ duyệt", "Chờ manager phê duyệt"),
    APPROVED("Đã duyệt", "Manager phê duyệt, sẵn sàng gửi"),
    SENT_TO_SUPPLIER("Đã gửi supplier", "Gửi tới nhà cung cấp"),
    GOODS_RECEIVED("Đã nhận hàng", "Hàng đã tới kho, kiểm tra chất lượng"),
    REJECTED("Từ chối", "Manager từ chối, quay lại soạn thảo"),
    CANCELLED("Đã hủy", "PO bị hủy (cuối cùng)"),
    CLOSED("Đã kết thúc", "Hoàn tất (nhận hàng, QC, thanh toán)");

    private final String displayName;
    private final String description;

}
