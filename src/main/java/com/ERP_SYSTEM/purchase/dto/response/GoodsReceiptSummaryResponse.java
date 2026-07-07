package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.GoodsReceiptStatus;
import com.ERP_SYSTEM.purchase.enums.InventoryImportStatus;
import com.ERP_SYSTEM.purchase.enums.QualityCheckStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record GoodsReceiptSummaryResponse(
        UUID id,
        String grNumber,
        String poNumber,
        String supplierName,
        LocalDateTime grDate,
        QualityCheckStatus qualityCheckStatus,
        InventoryImportStatus inventoryImportStatus,
        GoodsReceiptStatus status
) {
}
