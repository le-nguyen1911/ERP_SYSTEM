package com.ERP_SYSTEM.audit.entity;

import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.common.base.BaseEntity;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import io.hypersistence.utils.hibernate.type.json.JsonType;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.Type;

import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog extends BaseEntity {
    @Column(name = "entity_type", nullable = false, length = 50)
    private String entityType;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Enumerated(EnumType.STRING)
    @Column(name = "action", nullable = false, length = 30)
    private AuditAction action;

    @Type(JsonType.class)
    @Column(name = "old_value", columnDefinition = "jsonb")
    private String oldValue;

    @Type(JsonType.class)
    @Column(name = "new_value", columnDefinition = "jsonb")
    private String newValue;

    @Column(name = "performed_by_id", nullable = false)
    private UUID performedById;

    @Enumerated(EnumType.STRING)
    @Column(name = "module", nullable = false, length = 30)
    private SourceModule module;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
