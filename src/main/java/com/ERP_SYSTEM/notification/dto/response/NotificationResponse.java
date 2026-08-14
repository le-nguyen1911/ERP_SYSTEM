package com.ERP_SYSTEM.notification.dto.response;

import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificationResponse(
        UUID id,
        UUID recipientId,
        String title,
        String message,
        NotificationType type,
        SourceModule module,
        String referenceType,
        UUID referenceId,
        Boolean isRead,
        LocalDateTime readAt,
        LocalDateTime createdAt
) {
}
