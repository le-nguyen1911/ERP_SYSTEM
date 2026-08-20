package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public interface RoleService {
    RoleResponse createRole(CreateRoleRequest request);

    RoleResponse updateRole(UUID id, CreateRoleRequest request);

    void deleteRole(UUID id);

    RoleResponse addPermissionsToRole(UUID id, Set<String> permissions);

    RoleResponse removePermissionsFromRole(UUID id, Set<String> permissions);

    RoleResponse setPermissionsForRole(UUID id, Set<String> permissions);

    List<RoleResponse> getAllRoles();

    RoleResponse getRoleById(UUID id);
}
