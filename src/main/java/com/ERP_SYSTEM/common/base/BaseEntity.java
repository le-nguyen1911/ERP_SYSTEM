package com.ERP_SYSTEM.common.base;


import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

import java.time.LocalDateTime;

@Getter @Setter
@MappedSuperclass// đây là entity cha để các entity con kế thừa các field chuing
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @CreatedDate // chỉ set 1 lầ lúc tạo mới
    @Column(updatable = false)//sau khi tạo không update
    private LocalDateTime createdAt;

    @LastModifiedDate // luu moi khi save
    private LocalDateTime updatedAt;
}
