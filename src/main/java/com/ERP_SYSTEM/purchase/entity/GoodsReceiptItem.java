package com.ERP_SYSTEM.purchase.entity;


import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "goods_receipt_item")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceiptItem extends SoftDeleteBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "goods_receipt_id", nullable = false)
    private GoodsReceipt goodsReceipt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_item_id", nullable = false)
    private PurchaseOrderItem purchaseOrderItem;

    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity_accepted", nullable = false, precision = 19, scale = 4)
    private BigDecimal quantityAccepted;

    @Column(name = "quantity_rejected", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal quantityRejected = BigDecimal.ZERO;

    @Column(name = "batch_number", length = 50)
    private String batchNumber;

    @Column(name = "expiry_date")
    private LocalDate expiryDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;
}
