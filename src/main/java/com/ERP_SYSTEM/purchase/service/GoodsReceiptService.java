package com.ERP_SYSTEM.purchase.service;

import com.ERP_SYSTEM.purchase.dto.request.CreateGoodsReceiptRequest;
import com.ERP_SYSTEM.purchase.dto.request.QualityCheckRequest;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface GoodsReceiptService {
    GoodsReceiptDetailResponse create(CreateGoodsReceiptRequest request);

    GoodsReceiptDetailResponse getById(UUID id);

    Page<GoodsReceiptSummaryResponse> getAll(Pageable pageable);

    Page<GoodsReceiptSummaryResponse> getByPurchaseOrder(UUID purchaseOrderId, Pageable pageable);

    // Chuyển DRAFT -> RECEIVED
    GoodsReceiptDetailResponse markAsReceived(UUID id);

    // Thực hiện QC, nếu PASSED sẽ tự động trigger gọi Inventory Module
    GoodsReceiptDetailResponse performQualityCheck(UUID id, QualityCheckRequest request);

    // Cho phép gọi lại thủ công (hoặc scheduler gọi tự động) khi
    GoodsReceiptDetailResponse retryInventoryImport(UUID id);


    void retryAllFailedInventoryImports();

    void cancel(UUID id, String reason);
}
