package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.auth.dto.request.CreatePermissionRequest;
import com.ERP_SYSTEM.auth.dto.response.PermissionResponse;
import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.mapper.RoleMapper;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl  implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;


    @Override
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        if(permissionRepository.findByName(request.getName()).isPresent()){
            throw new RuntimeException("Permission đã tồn tại" + request.getName());
        }
        Permission permission = Permission.builder()
                .name(request.getName().toUpperCase())
                .description(request.getDescription())
                .build();
        permissionRepository.save(permission);
        return roleMapper.toPermissionResponse(permission);
    }

    @Override
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(roleMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }
}
