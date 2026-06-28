package com.ERP_SYSTEM.inventory.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.inventory.dto.request.CreateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateWarehouseRequest;
import com.ERP_SYSTEM.inventory.dto.response.WarehouseResponse;
import com.ERP_SYSTEM.inventory.service.WarehouseService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/warehouses")
@RequiredArgsConstructor
public class WarehouseController {
    private final WarehouseService warehouseService;

    @GetMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_VIEW')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllWarehouses() {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_VIEW')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> getWarehouseById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getById(id)));
    }

    @GetMapping("/active")
    @PreAuthorize("hasAuthority('WAREHOUSE_VIEW')")
    public ResponseEntity<ApiResponse<List<WarehouseResponse>>> getAllActiveWarehouses() {
        return ResponseEntity.ok(ApiResponse.success(warehouseService.getAllActive()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('WAREHOUSE_CREATE')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> createWarehouse(@Valid @RequestBody CreateWarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo kho thành công ", warehouseService.create(request)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_UPDATE')")
    public ResponseEntity<ApiResponse<WarehouseResponse>> updateWarehouse(@PathVariable UUID id, @Valid @RequestBody UpdateWarehouseRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật kho thành công", warehouseService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('WAREHOUSE_DELETE')")
    public ResponseEntity<ApiResponse<Void>> deleteWarehouse(@PathVariable UUID id) {
        warehouseService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá kho thành công", null));
    }

}
