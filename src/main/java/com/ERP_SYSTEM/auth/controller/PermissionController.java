package com.ERP_SYSTEM.auth.controller;

import com.ERP_SYSTEM.auth.dto.request.CreatePermissionRequest;
import com.ERP_SYSTEM.auth.dto.response.PermissionResponse;
import com.ERP_SYSTEM.auth.service.PermissionService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/permissions")
@RequiredArgsConstructor
public class PermissionController {

    private final PermissionService permissionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PermissionResponse>> createPermission(
            @Valid @RequestBody CreatePermissionRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Tạo quyền thành công",
                        permissionService.createPermission(request)
                )
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<PermissionResponse>>> getAllPermissions() {
        return ResponseEntity.ok(
                ApiResponse.success(permissionService.getAllPermissions())
        );
    }
}
