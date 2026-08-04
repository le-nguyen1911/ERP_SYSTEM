package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DeliverySummaryResponse(
        UUID id,
        String deliveryNumber,
        String soNumber,
        String customerName,
        LocalDateTime deliveryDate,
        InventoryExportStatus inventoryExportStatus,
        DeliveryStatus status
) {
}
