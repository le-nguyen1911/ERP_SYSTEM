package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record DeliveryDetailResponse(
        UUID id,
        String deliveryNumber,
        UUID salesOrderId,
        String soNumber,
        CustomerResponse customer,
        UUID warehouseId,
        LocalDateTime deliveryDate,
        UUID deliveredById,
        InventoryExportStatus inventoryExportStatus,
        String inventoryErrorMessage,
        LocalDateTime lastInventoryRetryAt,
        DeliveryStatus status,
        String rejectionReason,
        List<DeliveryItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}