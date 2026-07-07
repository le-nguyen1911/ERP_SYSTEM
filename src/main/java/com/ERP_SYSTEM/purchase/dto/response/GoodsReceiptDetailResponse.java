package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.GoodsReceiptStatus;
import com.ERP_SYSTEM.purchase.enums.InventoryImportStatus;
import com.ERP_SYSTEM.purchase.enums.QualityCheckStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record GoodsReceiptDetailResponse(
        UUID id,
        String grNumber,
        UUID purchaseOrderId,
        String poNumber,
        SupplierResponse supplier,
        UUID warehouseId,
        LocalDateTime grDate,
        UUID receivedById,
        QualityCheckStatus qualityCheckStatus,
        String qualityCheckNotes,
        UUID qualityCheckedById,
        LocalDateTime qualityCheckDate,
        InventoryImportStatus inventoryImportStatus,
        String inventoryErrorMessage,
        LocalDateTime lastInventoryRetryAt,
        GoodsReceiptStatus status,
        String rejectionReason,
        List<GoodsReceiptItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
