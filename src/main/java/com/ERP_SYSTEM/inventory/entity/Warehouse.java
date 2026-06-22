package com.ERP_SYSTEM.inventory.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "warehouses")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Warehouse extends BaseEntity {
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    private String location;
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

}
