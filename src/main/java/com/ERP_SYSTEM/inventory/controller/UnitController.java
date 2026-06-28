package com.ERP_SYSTEM.inventory.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.inventory.dto.request.CreateUnitRequest;
import com.ERP_SYSTEM.inventory.dto.response.UnitResponse;
import com.ERP_SYSTEM.inventory.service.UnitService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/units")
@RequiredArgsConstructor
public class UnitController {
    private final UnitService unitService;


    @GetMapping
    @PreAuthorize("hasAuthority('UNIT_VIEW')")
    public ResponseEntity<ApiResponse<List<UnitResponse>>> getAllUnit() {
        return ResponseEntity.ok(ApiResponse.success(unitService.getAll()));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_VIEW')")
    public ResponseEntity<ApiResponse<UnitResponse>> getUnitById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(unitService.getById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('UNIT_CREATE')")
    public ResponseEntity<ApiResponse<UnitResponse>> createUnit(@Valid @RequestBody CreateUnitRequest createUnitRequest) {
        UnitResponse created = unitService.create(createUnitRequest);
        return new ResponseEntity<>(ApiResponse.success(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_UPDATE')")
    public ResponseEntity<ApiResponse<UnitResponse>> updateUnit(@PathVariable UUID id, @Valid @RequestBody CreateUnitRequest createUnitRequest) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật đơn vị tình thành công", unitService.update(id, createUnitRequest)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('UNIT_CREATE')")
    public ResponseEntity<ApiResponse<UnitResponse>> deleteUnit(@PathVariable UUID id) {
        unitService.delete(id);
        return ResponseEntity.ok(ApiResponse.success("Xoá Thành công", null));
    }
}
