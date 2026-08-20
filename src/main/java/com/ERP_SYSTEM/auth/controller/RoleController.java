package com.ERP_SYSTEM.auth.controller;

import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;
import com.ERP_SYSTEM.auth.service.RoleService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse created = roleService.createRole(request);
        return new ResponseEntity<>(
                ApiResponse.success("Tạo vai trò thành công", created),
                HttpStatus.CREATED
        );
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<RoleResponse>>> getAllRoles() {
        return ResponseEntity.ok(
                ApiResponse.success(roleService.getAllRoles())
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success(roleService.getRoleById(id))
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable UUID id,
            @Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Cập nhật vai trò thành công", roleService.updateRole(id, request))
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable UUID id) {
        roleService.deleteRole(id);
        return ResponseEntity.ok(
                ApiResponse.success("Xóa vai trò thành công", null)
        );
    }

    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> addPermissions(
            @PathVariable UUID id,
            @RequestBody Set<String> permissions) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Thêm quyền thành công",
                        roleService.addPermissionsToRole(id, permissions)
                )
        );
    }

    @DeleteMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> removePermissions(
            @PathVariable UUID id,
            @RequestBody Set<String> permissions) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xóa quyền thành công",
                        roleService.removePermissionsFromRole(id, permissions)
                )
        );
    }

    @PutMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> setPermissions(
            @PathVariable UUID id,
            @RequestBody Set<String> permissions) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật danh sách quyền thành công",
                        roleService.setPermissionsForRole(id, permissions)
                )
        );
    }
}
