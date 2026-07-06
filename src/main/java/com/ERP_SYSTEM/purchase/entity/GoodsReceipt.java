package com.ERP_SYSTEM.purchase.entity;


import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.purchase.enums.GoodsReceiptStatus;
import com.ERP_SYSTEM.purchase.enums.InventoryImportStatus;
import com.ERP_SYSTEM.purchase.enums.QualityCheckStatus;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "goods_receipt")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GoodsReceipt extends SoftDeleteBaseEntity {

    @Column(name = "gr_number", nullable = false, unique = true, length = 50)
    private String grNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "purchase_order_id", nullable = false)
    private PurchaseOrder purchaseOrder;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "supplier_id", nullable = false)
    private Supplier supplier;

    @Column(name = "warehouse_id", nullable = false)
    private UUID warehouseId;

    @Column(name = "gr_date", nullable = false)
    @Builder.Default
    private LocalDateTime grDate = LocalDateTime.now();

    @Column(name = "received_by_id", nullable = false)
    private UUID receivedById;

    @Enumerated(EnumType.STRING)
    @Column(name = "quality_check_status", nullable = false, length = 30)
    @Builder.Default
    private QualityCheckStatus qualityCheckStatus = QualityCheckStatus.PENDING;

    @Column(name = "quality_check_notes", columnDefinition = "TEXT")
    private String qualityCheckNotes;

    @Column(name = "quality_checked_by_id")
    private UUID qualityCheckedById;

    @Column(name = "quality_check_date")
    private LocalDateTime qualityCheckDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "inventory_import_status", nullable = false, length = 30)
    @Builder.Default
    private InventoryImportStatus inventoryImportStatus = InventoryImportStatus.PENDING;

    @Column(name = "inventory_error_message", columnDefinition = "TEXT")
    private String inventoryErrorMessage;

    @Column(name = "last_inventory_retry_at")
    private LocalDateTime lastInventoryRetryAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private GoodsReceiptStatus status = GoodsReceiptStatus.DRAFT;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    @Version
    @Column(name = "version", nullable = false)
    @Builder.Default
    private Long version = 0L;

    @OneToMany(
            mappedBy = "goodsReceipt",
            fetch = FetchType.LAZY,
            cascade = CascadeType.ALL,
            orphanRemoval = true)
    @Builder.Default
    private List<GoodsReceiptItem> items = new ArrayList<>();

    public void addItem(GoodsReceiptItem item) {
        items.add(item);
        item.setGoodsReceipt(this);
    }
}
