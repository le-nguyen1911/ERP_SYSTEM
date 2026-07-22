package com.ERP_SYSTEM.sales.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "sales_order")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SalesOrder extends SoftDeleteBaseEntity {

    @Column(name = "so_number", nullable = false, unique = true, length = 50)
    private String soNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "so_date", nullable = false)
    @Builder.Default
    private LocalDateTime soDate = LocalDateTime.now();

    @Column(name = "delivery_date", nullable = false)
    private LocalDate deliveryDate;

    @Column(name = "currency", nullable = false, length = 3)
    @Builder.Default
    private String currency = "VND";

    @Column(name = "subtotal", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "tax_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_percentage", nullable = false, precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxPercentage = new BigDecimal("10.00");

    @Column(name = "shipping_cost", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal shippingCost = BigDecimal.ZERO;

    @Column(name = "discount_amount", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "grand_total", nullable = false, precision = 19, scale = 4)
    @Builder.Default
    private BigDecimal grandTotal = BigDecimal.ZERO;

    @Column(name = "payment_terms", length = 100)
    private String paymentTerms;

    @Column(name = "shipping_address", columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private SalesOrderStatus status = SalesOrderStatus.DRAFT;

    @Column(name = "approved_by_id")
    private UUID approvedById;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;

    @Column(name = "cancelled_by_id")
    private UUID cancelledById;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(
            mappedBy = "salesOrder",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<SalesOrderItem> items = new ArrayList<>();

    public void addItem(SalesOrderItem item) {
        items.add(item);
        item.setSalesOrder(this);
    }

    public void removeItem(SalesOrderItem item) {
        items.remove(item);
        item.setSalesOrder(null);
    }
}
