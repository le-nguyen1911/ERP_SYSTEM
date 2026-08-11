package com.ERP_SYSTEM.sales.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.sales.dto.request.CancelDeliveryRequest;
import com.ERP_SYSTEM.sales.dto.request.CreateDeliveryRequest;
import com.ERP_SYSTEM.sales.dto.response.DeliveryDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.DeliverySummaryResponse;
import com.ERP_SYSTEM.sales.service.DeliveryService;
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
@RequestMapping("api/v1/deliveries")
@RequiredArgsConstructor
public class DeliveryController {
    private final DeliveryService deliveryService;

    @PostMapping
    @PreAuthorize("hasAuthority('DELIVERY_CREATE')")
    public ResponseEntity<ApiResponse<DeliveryDetailResponse>> create(@Valid @RequestBody CreateDeliveryRequest request) {
        DeliveryDetailResponse response = deliveryService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success("Tạo phiếu giao hàng thành công", response));
    }

    @GetMapping("/id")
    @PreAuthorize("hasAuthority('DELIVERY_VIEW')")
    public ResponseEntity<ApiResponse<DeliveryDetailResponse>> findById(@PathVariable UUID id) {
        DeliveryDetailResponse response = deliveryService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('DELIVERY_VIEW')")
    public ResponseEntity<ApiResponse<Page<DeliverySummaryResponse>>> getAll(Pageable pageable) {
        Page<DeliverySummaryResponse> response = deliveryService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/{id}/retry-inventory-export")
    @PreAuthorize("hasAuthority('DELIVERY_EXPORT')")
    public ResponseEntity<ApiResponse<DeliveryDetailResponse>> retryInventoryExport(
            @PathVariable UUID id) {
        DeliveryDetailResponse response = deliveryService.retryInventoryExport(id);
        return ResponseEntity.ok(ApiResponse.success("Thử lại xuất kho thành công", response));
    }

    @PostMapping("/{id}/mark-as-delivered")
    @PreAuthorize("hasAuthority('DELIVERY_CREATE')")
    public ResponseEntity<ApiResponse<DeliveryDetailResponse>> markAsDelivered(
            @PathVariable UUID id) {
        DeliveryDetailResponse response = deliveryService.markAsDelivered(id);
        return ResponseEntity.ok(ApiResponse.success("Xác nhận đã giao hàng thành công", response));
    }
    
    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority('DELIVERY_CREATE')")
    public ResponseEntity<ApiResponse<Void>> cancel(
            @PathVariable UUID id,
            @Valid @RequestBody CancelDeliveryRequest request) {
        deliveryService.cancel(id, request.reason());
        return ResponseEntity.ok(ApiResponse.success("Hủy phiếu giao hàng thành công", null));
    }
}
