package com.ERP_SYSTEM.purchase.service;

import com.ERP_SYSTEM.purchase.dto.request.*;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface PurchaseOrderService {
    PurchaseOrderDetailResponse create(CreatePurchaseOrderRequest request);

    PurchaseOrderDetailResponse update(UUID id, UpdatePurchaseOrderRequest request);

    PurchaseOrderDetailResponse getById(UUID id);

    Page<PurchaseOrderSummaryResponse> getAll(Pageable pageable);

    Page<PurchaseOrderSummaryResponse> search(PurchaseOrderSearchRequest searchRequest, Pageable pageable);

    PurchaseOrderDetailResponse addItem(UUID poId, AddPurchaseOrderItemRequest request);

    PurchaseOrderDetailResponse updateItem(UUID poId, UUID itemId, UpdatePurchaseOrderItemRequest request);

    PurchaseOrderDetailResponse removeItem(UUID poId, UUID itemId);

    PurchaseOrderDetailResponse submitForApproval(UUID id);

    PurchaseOrderDetailResponse approve(UUID id, ApprovePurchaseOrderRequest request);

    PurchaseOrderDetailResponse reject(UUID id, RejectPurchaseOrderRequest request);

    PurchaseOrderDetailResponse sendToSupplier(UUID id);

    PurchaseOrderDetailResponse cancel(UUID id, CancelPurchaseOrderRequest request);

    PurchaseOrderDetailResponse close(UUID id);

    void delete(UUID id);
}
