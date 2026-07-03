package com.ERP_SYSTEM.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum GoodsReceiptStatus {

    DRAFT("Bản nháp", "Đang soạn phiếu nhận"),
    RECEIVED("Đã nhận", "Hàng đã tới, chờ QC"),
    QC_PASSED("QC Đạt", "Kiểm tra chất lượng OK, sẵn sàng import"),
    QC_FAILED("QC Không đạt", "Kiểm tra chất lượng thất bại, cần trả lại"),
    IMPORTED("Đã import", "Import vào Inventory thành công ✓"),
    CANCELLED("Đã hủy", "Phiếu nhận bị hủy");


    private final String displayName;
    private final String description;

}
