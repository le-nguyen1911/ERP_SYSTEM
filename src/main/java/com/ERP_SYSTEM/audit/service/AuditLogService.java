package com.ERP_SYSTEM.audit.service;

import com.ERP_SYSTEM.audit.dto.AuditLogResponse;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface AuditLogService {
    Page<AuditLogResponse> getByEntity(String entityType, UUID entityId, Pageable pageable);

    Page<AuditLogResponse> getByUser(UUID performedById, Pageable pageable);

    Page<AuditLogResponse> getByModule(SourceModule module, Pageable pageable);

    void logSystemEvent(String entityType, UUID entityId, AuditAction action,
                        UUID performedById, SourceModule module, String description);
}
