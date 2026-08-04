package com.ERP_SYSTEM.sales.dto.response;

import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record SalesOrderDetailResponse(
        UUID id,
        String soNumber,
        CustomerResponse customer,
        UUID warehouseId,
        LocalDateTime soDate,
        LocalDate deliveryDate,
        String currency,
        BigDecimal subtotal,
        BigDecimal taxAmount,
        BigDecimal taxPercentage,
        BigDecimal shippingCost,
        BigDecimal discountAmount,
        BigDecimal grandTotal,
        String paymentTerms,
        String shippingAddress,
        String notes,
        SalesOrderStatus status,
        UUID approvedById,
        LocalDateTime approvalDate,
        UUID cancelledById,
        LocalDateTime cancelledAt,
        String cancellationReason,
        String rejectionReason,
        List<SalesOrderItemResponse> items,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
