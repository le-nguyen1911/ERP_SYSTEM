package com.ERP_SYSTEM.sales.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "delivery_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryItem extends SoftDeleteBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "delivery_id", nullable = false)
    private Delivery delivery;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_item_id", nullable = false)
    private SalesOrderItem salesOrderItem;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity_delivered", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityDelivered;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
