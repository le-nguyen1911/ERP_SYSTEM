package com.ERP_SYSTEM.audit.dto;

import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;

import java.time.LocalDateTime;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        String entityType,
        UUID entityId,
        AuditAction action,
        String oldValue,
        String newValue,
        UUID performedById,
        SourceModule module,
        LocalDateTime createdAt
) {
}
