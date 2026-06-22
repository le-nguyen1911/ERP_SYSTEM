package com.ERP_SYSTEM.inventory.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "units")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Unit extends BaseEntity {
    @Column(nullable = false, unique = true, length = 50)
    private String name;

    private String description;
}
