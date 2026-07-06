package com.ERP_SYSTEM.purchase.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purchase_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseOrder extends BaseEntity {

    @Column(
            name = "po_number",
            nullable = false,
            unique = true,
            length = 50,
            updatable = false
    )
    private String poNumber;

    @Column(
            name = "supplier_id",
            nullable = false,
            updatable = false
    )
    private UUID supplierId;

    @Column(
            name = "warehouse_id",
            nullable = false
    )
    private UUID warehouseId;

    @Column(name = "requisition_id")
    private UUID requisitionId;

    @Column(
            name = "po_date",
            nullable = false
    )
    @Builder.Default
    private LocalDateTime poDate = LocalDateTime.now();

    @Column(
            name = "delivery_date",
            nullable = false
    )
    private LocalDate deliveryDate;

    @Column(
            name = "currency",
            nullable = false,
            length = 3
    )
    @Builder.Default
    private String currency = "VND";

    @Column(
            name = "subtotal",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(
            name = "tax_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(
            name = "tax_percentage",
            nullable = false,
            precision = 5,
            scale = 2
    )
    @Builder.Default
    private BigDecimal taxPercentage = new BigDecimal("10.00");

    @Column(
            name = "shipping_cost",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(
            name = "discount_amount",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(
            name = "grand_total",
            nullable = false,
            precision = 19,
            scale = 4
    )
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(
            name = "payment_terms",
            length = 100
    )
    private String paymentTerms;

    @Column(
            name = "incoterms",
            length = 50
    )
    private String incoterms;

    @Column(name = "notes")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    @Builder.Default
    private PurchaseOrderStatus status = PurchaseOrderStatus.DRAFT;

    @Column(name = "approved_by_id")
    private UUID approvedBy;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "cancelled_by_id")
    private UUID cancelledBy;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason")
    private String cancellationReason;

    @Version
    @Column(
            name = "version",
            nullable = false
    )
    @Builder.Default
    private Long version = 0L;

    @Column(
            name = "is_deleted",
            nullable = false
    )
    @Builder.Default
    private Boolean isDeleted = false;

    @OneToMany(
            mappedBy = "purchaseOrder",
            cascade = CascadeType.ALL,
            orphanRemoval = true,
            fetch = FetchType.LAZY
    )
    @Builder.Default
    private List<PurchaseOrderItem> items = new ArrayList<>();

    //helper method

    public boolean isEditable() {
        return status == PurchaseOrderStatus.DRAFT;
    }

    public boolean isApprovable() {
        return status == PurchaseOrderStatus.APPROVED;
    }

    public boolean isCancellable() {
        return status == PurchaseOrderStatus.CANCELLED;
    }

    public void approve(UUID approvedByUserId) {
        if (!isApprovable()) {
            throw new IllegalStateException("");
        }
    }
}
