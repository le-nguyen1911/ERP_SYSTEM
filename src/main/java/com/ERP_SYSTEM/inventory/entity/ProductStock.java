package com.ERP_SYSTEM.inventory.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_stocks",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"product_id", "warehouse_id"}
        ))
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProductStock extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id", nullable = false)
    private Warehouse warehouse;

    @Builder.Default
    @Column(nullable = false)
    private Integer quantity = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer minQuantity = 0;

}
