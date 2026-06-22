package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);
    // Tạo role mới với danh sách permission

    RoleResponse addPermissionsToRole(UUID id, Set<String> permissions);
    // Thêm permission vào role đã có

    RoleResponse removePermissionsFromRole(UUID id, Set<String> permissions);
    // Xóa permission khỏi role

    List<RoleResponse> getAllRoles();
    // Lấy danh sách tất cả role + permissions của mỗi role

    RoleResponse getRoleById(UUID id);
}
