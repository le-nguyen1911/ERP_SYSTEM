package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.CreatePermissionRequest;
import com.ERP_SYSTEM.auth.dto.response.PermissionResponse;

import java.util.List;

public interface PermissionService {
    PermissionResponse createPermission(CreatePermissionRequest request);
    List<PermissionResponse> getAllPermissions();
}
