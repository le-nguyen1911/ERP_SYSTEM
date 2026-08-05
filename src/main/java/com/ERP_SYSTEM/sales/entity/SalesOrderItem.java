package com.ERP_SYSTEM.sales.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderItemStatus;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "sales_order_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class SalesOrderItem extends SoftDeleteBaseEntity {
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "product_code", nullable = false, length = 50)
    private String productCode;

    @Column(name = "product_name", nullable = false, length = 255)
    private String productName;

    @Column(name = "product_unit", nullable = false, length = 20)
    private String productUnit;

    @Column(name = "line_number", nullable = false)
    private Integer lineNumber;

    @Column(name = "quantity", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantity;

    @Column(name = "unit_price", nullable = false, precision = 19, scale = 4)
    private BigDecimal unitPrice;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "delivered_quantity", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal deliveredQuantity = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SalesOrderItemStatus status = SalesOrderItemStatus.PENDING;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Transient
    public BigDecimal getLineTotal() {
        return quantity.multiply(unitPrice);
    }
}
