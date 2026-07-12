package com.ERP_SYSTEM.purchase.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.purchase.dto.request.*;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderSummaryResponse;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;
import com.ERP_SYSTEM.purchase.service.PurchaseOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/purchase-orders")
@RequiredArgsConstructor
public class PurchaseOrderController {

    private final PurchaseOrderService purchaseOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> create(
            @Valid @RequestBody CreatePurchaseOrderRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo đơn đặt hàng thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdatePurchaseOrderRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn đặt hàng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE') or hasAuthority('PURCHASE_APPROVE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> getById(@PathVariable UUID id) {
        PurchaseOrderDetailResponse response = purchaseOrderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE') or hasAuthority('PURCHASE_APPROVE')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderSummaryResponse>>> getAll(Pageable pageable) {
        Page<PurchaseOrderSummaryResponse> response = purchaseOrderService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE') or hasAuthority('PURCHASE_APPROVE')")
    public ResponseEntity<ApiResponse<Page<PurchaseOrderSummaryResponse>>> search(
            @RequestParam(required = false) UUID supplierId,
            @RequestParam(required = false) PurchaseOrderStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Pageable pageable) {
        PurchaseOrderSearchRequest searchRequest =
                new PurchaseOrderSearchRequest(supplierId, status, fromDate, toDate);
        Page<PurchaseOrderSummaryResponse> response = purchaseOrderService.search(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }


    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> addItem(
            @PathVariable UUID id,
            @Valid @RequestBody AddPurchaseOrderItemRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Thêm sản phẩm vào đơn hàng thành công", response));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdatePurchaseOrderItemRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.updateItem(id, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", response));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        PurchaseOrderDetailResponse response = purchaseOrderService.removeItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi đơn hàng thành công", response));
    }

    @PostMapping("/{id}/submit-for-approval")
    @PreAuthorize("hasAuthority('PURCHASE_CREATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> submitForApproval(
            @PathVariable UUID id) {
        PurchaseOrderDetailResponse response = purchaseOrderService.submitForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Gửi duyệt đơn đặt hàng thành công", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('PURCHASE_APPROVE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) ApprovePurchaseOrderRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.approve(
                id, request != null ? request : new ApprovePurchaseOrderRequest());
        return ResponseEntity.ok(ApiResponse.success("Duyệt đơn đặt hàng thành công", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('PURCHASE_APPROVE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectPurchaseOrderRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.reject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối đơn đặt hàng thành công", response));
    }

    @PostMapping("/{id}/send-to-supplier")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> sendToSupplier(
            @PathVariable UUID id) {
        PurchaseOrderDetailResponse response = purchaseOrderService.sendToSupplier(id);
        return ResponseEntity.ok(ApiResponse.success("Gửi đơn hàng cho nhà cung cấp thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('PURCHASE_CANCEL')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelPurchaseOrderRequest request) {
        PurchaseOrderDetailResponse response = purchaseOrderService.cancel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn đặt hàng thành công", response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<PurchaseOrderDetailResponse>> close(@PathVariable UUID id) {
        PurchaseOrderDetailResponse response = purchaseOrderService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Đóng đơn đặt hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('PURCHASE_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        purchaseOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá đơn đặt hàng thành công", null));
    }
}