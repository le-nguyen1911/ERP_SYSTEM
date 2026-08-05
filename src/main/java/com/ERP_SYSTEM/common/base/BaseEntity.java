package com.ERP_SYSTEM.common.base;


import com.ERP_SYSTEM.common.uuid.UuidV7Generator;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public abstract class BaseEntity {

    @Id
    @GeneratedValue(generator = "uuid-v7")
    @GenericGenerator(
            name = "uuid-v7",
            type = UuidV7Generator.class
    )
    private UUID id;

    @CreatedDate // chỉ set 1 lầ lúc tạo mới
    @Column(updatable = false)//sau khi tạo không update
    private LocalDateTime createdAt;

    @LastModifiedDate // luu moi khi save
    private LocalDateTime updatedAt;
}
