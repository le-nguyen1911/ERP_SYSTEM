package com.ERP_SYSTEM.sales.event;

import java.util.UUID;

public record DeliveryExportFailedEvent(
        UUID deliveryId,
        String deliveryNumber,
        UUID deliveredById,
        String errorMessage
) {
}
