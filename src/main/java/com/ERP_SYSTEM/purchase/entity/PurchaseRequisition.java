package com.ERP_SYSTEM.purchase.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.purchase.enums.RequisitionStatus;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "purchase_requisition")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PurchaseRequisition extends SoftDeleteBaseEntity {
    @Column(name = "pr_number", nullable = false, unique = true, length = 50)
    private String prNumber;

    @Column(name = "requested_by_id", nullable = false)
    private UUID requestedById;

    @Column(name = "required_date", nullable = false)
    private LocalDate requiredDate;

    @Column(name = "purpose", columnDefinition = "TEXT")
    private String purpose;

    @Column(name = "purchase_order_id")
    private UUID purchaseOrderId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    @Builder.Default
    private RequisitionStatus status = RequisitionStatus.DRAFT;

    @Column(name = "estimated_total", precision = 19, scale = 4)
    private BigDecimal estimatedTotal;

    @Column(name = "approved_by_id")
    private UUID approvedById;

    @Column(name = "approval_date")
    private LocalDateTime approvalDate;


}
