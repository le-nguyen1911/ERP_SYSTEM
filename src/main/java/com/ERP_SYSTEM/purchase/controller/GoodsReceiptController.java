package com.ERP_SYSTEM.purchase.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.purchase.dto.request.CancelGoodsReceiptRequest;
import com.ERP_SYSTEM.purchase.dto.request.CreateGoodsReceiptRequest;
import com.ERP_SYSTEM.purchase.dto.request.QualityCheckRequest;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptSummaryResponse;
import com.ERP_SYSTEM.purchase.service.GoodsReceiptService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/goods-receipts")
@RequiredArgsConstructor
public class GoodsReceiptController {

    private final GoodsReceiptService goodsReceiptService;

    @PostMapping
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_CREATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> create(
            @Valid @RequestBody CreateGoodsReceiptRequest request) {
        GoodsReceiptDetailResponse response = goodsReceiptService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo phiếu nhận hàng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_VIEW')")
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> getById(@PathVariable UUID id) {
        GoodsReceiptDetailResponse response = goodsReceiptService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_VIEW')")
    public ResponseEntity<ApiResponse<Page<GoodsReceiptSummaryResponse>>> getAll(Pageable pageable) {
        Page<GoodsReceiptSummaryResponse> response = goodsReceiptService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/by-purchase-order/{purchaseOrderId}")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_VIEW')")
    public ResponseEntity<ApiResponse<Page<GoodsReceiptSummaryResponse>>> getByPurchaseOrder(
            @PathVariable UUID purchaseOrderId, Pageable pageable) {
        Page<GoodsReceiptSummaryResponse> response =
                goodsReceiptService.getByPurchaseOrder(purchaseOrderId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/mark-as-received")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_CREATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> markAsReceived(
            @PathVariable UUID id) {
        GoodsReceiptDetailResponse response = goodsReceiptService.markAsReceived(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận nhận hàng thành công", response));
    }

    @PostMapping("/{id}/quality-check")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_CREATE')")
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> performQualityCheck(
            @PathVariable UUID id,
            @Valid @RequestBody QualityCheckRequest request) {
        GoodsReceiptDetailResponse response = goodsReceiptService.performQualityCheck(id, request);
        return ResponseEntity.ok(ApiResponse.success("Kiểm tra chất lượng hoàn tất", response));
    }

    @PostMapping("/{id}/retry-inventory-import")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_IMPORT')")
    public ResponseEntity<ApiResponse<GoodsReceiptDetailResponse>> retryInventoryImport(
            @PathVariable UUID id) {
        GoodsReceiptDetailResponse response = goodsReceiptService.retryInventoryImport(id);
        return ResponseEntity.ok(ApiResponse.success("Thử lại nhập kho thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('GOODS_RECEIPT_CREATE')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelGoodsReceiptRequest request) {
        goodsReceiptService.cancel(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Hủy phiếu nhận hàng thành công", null));
    }
}