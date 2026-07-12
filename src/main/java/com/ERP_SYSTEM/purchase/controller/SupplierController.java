package com.ERP_SYSTEM.purchase.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.purchase.dto.request.CreateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.request.SupplierSearchRequest;
import com.ERP_SYSTEM.purchase.dto.request.UpdateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.response.SupplierResponse;
import com.ERP_SYSTEM.purchase.service.SupplierService;
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
@RequestMapping("/api/v1/suppliers")
@RequiredArgsConstructor
public class SupplierController {

    private final SupplierService supplierService;

    @PostMapping
    @PreAuthorize("hasAuthority('SUPPLIER_CREATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> create(
            @Valid @RequestBody CreateSupplierRequest request) {
        SupplierResponse response = supplierService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo nhà cung cấp thành công", response));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    public ResponseEntity<ApiResponse<SupplierResponse>> update(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateSupplierRequest request) {
        SupplierResponse response = supplierService.update(id, request);
        return ResponseEntity.ok(ApiResponse.success("Cập nhật nhà cung cấp thành công", response));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW')")
    public ResponseEntity<ApiResponse<SupplierResponse>> getById(@PathVariable UUID id) {
        SupplierResponse response = supplierService.getById(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW')")
    public ResponseEntity<ApiResponse<Page<SupplierResponse>>> getAll(Pageable pageable) {
        Page<SupplierResponse> response = supplierService.getAll(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('SUPPLIER_VIEW')")
    public ResponseEntity<ApiResponse<Page<SupplierResponse>>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) com.ERP_SYSTEM.purchase.enums.SupplierStatus status,
            Pageable pageable) {
        SupplierSearchRequest searchRequest = new SupplierSearchRequest(keyword, status);
        Page<SupplierResponse> response = supplierService.search(searchRequest, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SUPPLIER_UPDATE')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        supplierService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá nhà cung cấp thành công", null));
    }
}