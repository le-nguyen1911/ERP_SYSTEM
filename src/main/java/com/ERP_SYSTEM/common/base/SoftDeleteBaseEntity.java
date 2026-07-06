package com.ERP_SYSTEM.common.base;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@MappedSuperclass
public abstract class SoftDeleteBaseEntity extends BaseEntity {
    
    @Column(name = "is_deleted", nullable = false)
    private Boolean isDeleted = false;
}
