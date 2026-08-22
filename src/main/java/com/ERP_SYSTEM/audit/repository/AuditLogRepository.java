package com.ERP_SYSTEM.audit.repository;

import com.ERP_SYSTEM.audit.entity.AuditLog;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {
    Page<AuditLog> findAllByOrderByCreatedAtDesc(Pageable pageable);

    Page<AuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(
            String entityType, UUID entityId, Pageable pageable);

    Page<AuditLog> findByPerformedByIdOrderByCreatedAtDesc(UUID performedById, Pageable pageable);

    Page<AuditLog> findByModuleOrderByCreatedAtDesc(SourceModule module, Pageable pageable);
}

