package com.ERP_SYSTEM.purchase.dto.response;

import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record PurchaseOrderDetailResponse(
        UUID id,
        String poNumber,
        SupplierResponse supplier,
        UUID warehouseId,
        LocalDateTime poDate,
        LocalDate deliveryDate,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal taxPercentage,
        BigDecimal shippingCost,
        BigDecimal discountAmount,
        BigDecimal grandTotal,
        String paymentTerms,
        String incoterms,
        String notes,
        PurchaseOrderStatus status,
        UUID approvedById,
        LocalDateTime approvalDate,
        UUID cancelledById,
        LocalDateTime cancelledAt,
        String cancellationReason,
        String rejectionReason,
        List<PurchaseOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}