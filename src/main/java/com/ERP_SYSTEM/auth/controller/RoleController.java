package com.ERP_SYSTEM.auth.controller;

import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;
import com.ERP_SYSTEM.auth.service.RoleService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;
import java.util.UUID;

@Controller
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> createRoled(@Valid @RequestBody CreateRoleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Tạo role thành công", roleService.createRole(request))
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


    @PostMapping("/{id}/permissions")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<RoleResponse>> addPermissions(@PathVariable UUID id, @Valid @RequestBody Set<String> permissions) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Thêm Permission thành công",
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
}
