package com.ERP_SYSTEM.audit.service.Impl;

import com.ERP_SYSTEM.audit.dto.AuditLogResponse;
import com.ERP_SYSTEM.audit.entity.AuditLog;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.audit.repository.AuditLogRepository;
import com.ERP_SYSTEM.audit.service.AuditLogService;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuditLogServiceImpl implements AuditLogService {

    private final AuditLogRepository auditLogRepository;

    @Override
    public Page<AuditLogResponse> getAll(Pageable pageable) {
        return auditLogRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getByEntity(String entityType, UUID entityId, Pageable pageable) {
        return auditLogRepository
                .findByEntityTypeAndEntityIdOrderByCreatedAtDesc(entityType, entityId, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getByUser(UUID performedById, Pageable pageable) {
        return auditLogRepository.findByPerformedByIdOrderByCreatedAtDesc(performedById, pageable)
                .map(this::toResponse);
    }

    @Override
    public Page<AuditLogResponse> getByModule(SourceModule module, Pageable pageable) {
        return auditLogRepository.findByModuleOrderByCreatedAtDesc(module, pageable)
                .map(this::toResponse);
    }

    @Override
    @Transactional
    public void logSystemEvent(String entityType, UUID entityId, AuditAction action,
                               UUID performedById, SourceModule module, String description) {
        AuditLog log = AuditLog.builder()
                .entityType(entityType)
                .entityId(entityId)
                .action(action)
                .performedById(performedById != null ? performedById
                        : UUID.fromString("00000000-0000-0000-0000-000000000000"))
                .module(module)
                .description(description)
                .build();
        auditLogRepository.save(log);
    }

    private AuditLogResponse toResponse(com.ERP_SYSTEM.audit.entity.AuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getEntityType(), log.getEntityId(), log.getAction(),
                log.getOldValue(), log.getNewValue(), log.getPerformedById(),
                log.getModule(), log.getCreatedAt()
        );
    }
}

