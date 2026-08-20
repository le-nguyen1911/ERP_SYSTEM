package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.auth.dto.request.CreatePermissionRequest;
import com.ERP_SYSTEM.auth.dto.response.PermissionResponse;
import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.mapper.RoleMapper;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.service.PermissionService;
import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PermissionServiceImpl implements PermissionService {
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Auditable(
            entityClass = Permission.class,
            entityType = "Permission",
            action = AuditAction.CREATE,
            module = SourceModule.AUTH,
            idExpression = "#result.id"
    )
    @Override
    @Transactional
    public PermissionResponse createPermission(CreatePermissionRequest request) {
        String permName = request.getName().trim().toUpperCase();
        if (permissionRepository.findByName(permName).isPresent()) {
            throw new DuplicateResourceException("Quyền " + permName + " đã tồn tại trong hệ thống");
        }
        Permission permission = Permission.builder()
                .name(permName)
                .description(request.getDescription())
                .build();
        permissionRepository.save(permission);
        return roleMapper.toPermissionResponse(permission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PermissionResponse> getAllPermissions() {
        return permissionRepository.findAll()
                .stream()
                .map(roleMapper::toPermissionResponse)
                .collect(Collectors.toList());
    }
}
