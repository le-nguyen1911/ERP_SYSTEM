package com.ERP_SYSTEM.sales.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.sales.dto.request.*;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderSummaryResponse;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import com.ERP_SYSTEM.sales.service.SalesOrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/sales-orders")
@RequiredArgsConstructor
public class SalesOrderController {
    private final SalesOrderService salesOrderService;

    @PostMapping
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> create(@Valid @RequestBody CreateSalesOrderRequest request) {
        SalesOrderDetailResponse response = salesOrderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo đơn bán hàng thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> update(@PathVariable UUID id, @Valid @RequestBody UpdateSalesOrderRequest request) {
        SalesOrderDetailResponse response = salesOrderService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật thông tin đơn bán hàng thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> getById(@PathVariable UUID id) {
        SalesOrderDetailResponse response = salesOrderService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<ApiResponse<Page<SalesOrderSummaryResponse>>> getAll(Pageable pageable) {
        Page<SalesOrderSummaryResponse> responses = salesOrderService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SALES_VIEW')")
    public ResponseEntity<ApiResponse<Page<SalesOrderSummaryResponse>>> search(
            @RequestParam(required = false) UUID customerId,
            @RequestParam(required = false) SalesOrderStatus status,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate,
            Pageable pageable) {
        SalesOrderSearchRequest searchRequest = new SalesOrderSearchRequest(customerId, status, fromDate, toDate);
        Page<SalesOrderSummaryResponse> responses = salesOrderService.search(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{id}/items")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> addItems(@PathVariable UUID id, @Valid @RequestBody AddSalesOrderItemRequest request) {
        SalesOrderDetailResponse response = salesOrderService.addItem(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Thêm sản phẩm vào đơn hàng thành công", response));
    }

    @PutMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> updateItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId,
            @Valid @RequestBody UpdateSalesOrderItemRequest request) {
        SalesOrderDetailResponse response = salesOrderService.updateItem(id, itemId, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", response));
    }

    @DeleteMapping("/{id}/items/{itemId}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> removeItem(
            @PathVariable UUID id,
            @PathVariable UUID itemId) {
        SalesOrderDetailResponse response = salesOrderService.removeItem(id, itemId);
        return ResponseEntity.ok(ApiResponse.success("Xóa sản phẩm khỏi đơn hàng thành công", response));
    }

    @PostMapping("/{id}/submit-for-approval")
    @PreAuthorize("hasAuthority('SALES_CREATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> submitForApproval(
            @PathVariable UUID id) {
        SalesOrderDetailResponse response = salesOrderService.submitForApproval(id);
        return ResponseEntity.ok(ApiResponse.success("Gửi duyệt đơn bán hàng thành công", response));
    }

    @PostMapping("/{id}/approve")
    @PreAuthorize("hasAuthority('SALES_APPROVE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> approve(
            @PathVariable UUID id,
            @RequestBody(required = false) ApproveSalesOrderRequest request) {
        SalesOrderDetailResponse response = salesOrderService.approve(
                id, request != null ? request : new ApproveSalesOrderRequest());
        return ResponseEntity.ok(ApiResponse.success("Duyệt đơn bán hàng thành công", response));
    }

    @PostMapping("/{id}/reject")
    @PreAuthorize("hasAuthority('SALES_APPROVE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> reject(
            @PathVariable UUID id,
            @Valid @RequestBody RejectSalesOrderRequest request) {
        SalesOrderDetailResponse response = salesOrderService.reject(id, request);
        return ResponseEntity.ok(ApiResponse.success("Từ chối đơn bán hàng thành công", response));
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> confirm(@PathVariable UUID id) {
        SalesOrderDetailResponse response = salesOrderService.confirm(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đơn bán hàng thành công", response));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('SALES_CANCEL')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelSalesOrderRequest request) {
        SalesOrderDetailResponse response = salesOrderService.cancel(id, request);
        return ResponseEntity.ok(ApiResponse.success("Hủy đơn bán hàng thành công", response));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<SalesOrderDetailResponse>> close(@PathVariable UUID id) {
        SalesOrderDetailResponse response = salesOrderService.close(id);
        return ResponseEntity.ok(ApiResponse.success("Đóng đơn bán hàng thành công", response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SALES_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        salesOrderService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá đơn bán hàng thành công", null));
    }
}
