package com.ERP_SYSTEM.sales.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "delivery")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Delivery extends SoftDeleteBaseEntity {

    @Column(name = "delivery_number", nullable = false, unique = true, length = 50)
    private String deliveryNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sales_order_id", nullable = false)
    private SalesOrder salesOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "delivery_date", nullable = false)
    @Builder.Default
    private LocalDateTime deliveryDate = LocalDateTime.now();

    @Column(name = "delivered_by_id", nullable = false)
    private UUID deliveredById;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_export_status", nullable = false, length = 30)
    @Builder.Default
    private InventoryExportStatus inventoryExportStatus = InventoryExportStatus.PENDING;

    @Column(name = "inventory_error_message", columnDefinition = "TEXT")
    private String inventoryErrorMessage;

    @Column(name = "last_inventory_retry_at")
    private LocalDateTime lastInventoryRetryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private DeliveryStatus status = DeliveryStatus.DRAFT;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(
            mappedBy = "delivery",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<DeliveryItem> items = new ArrayList<>();

    public void addItem(DeliveryItem item) {
        items.add(item);
        item.setDelivery(this);
    }
}
