package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;
import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.mapper.RoleMapper;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import com.ERP_SYSTEM.auth.service.RoleService;
import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.CREATE,
            module = SourceModule.AUTH,
            idExpression = "#result.id"
    )
    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        String roleName = request.getName().trim();
        if (roleRepository.findByName(roleName).isPresent()) {
            throw new DuplicateResourceException("Vai trò " + roleName + " đã tồn tại trong hệ thống");
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findByNameIn(request.getPermissions());
        }
        Role role = Role.builder()
                .name(roleName)
                .description(request.getDescription())
                .permissions(permissions)
                .build();
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.UPDATE,
            module = SourceModule.AUTH
    )
    @Override
    @Transactional
    public RoleResponse updateRole(UUID id, CreateRoleRequest request) {
        Role role = findRoleById(id);
        String newName = request.getName().trim();

        if (!role.getName().equalsIgnoreCase(newName)) {
            if (roleRepository.findByName(newName).isPresent()) {
                throw new DuplicateResourceException("Vai trò " + newName + " đã tồn tại trong hệ thống");
            }
            if ("ADMIN".equalsIgnoreCase(role.getName())) {
                throw new RuntimeException("Không thể đổi tên vai trò quản trị hệ thống ADMIN");
            }
            role.setName(newName);
        }

        role.setDescription(request.getDescription());

        if (request.getPermissions() != null) {
            Set<Permission> permissions = permissionRepository.findByNameIn(request.getPermissions());
            role.setPermissions(new HashSet<>(permissions));
        }

        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.DELETE,
            module = SourceModule.AUTH
    )
    @Override
    @Transactional
    public void deleteRole(UUID id) {
        Role role = findRoleById(id);

        if ("ADMIN".equalsIgnoreCase(role.getName())) {
            throw new RuntimeException("Không thể xóa vai trò quản trị hệ thống ADMIN");
        }

        if (role.getUsers() != null && !role.getUsers().isEmpty()) {
            throw new RuntimeException("Không thể xóa vai trò đang có " + role.getUsers().size() + " người dùng được gán");
        }

        role.getPermissions().clear();
        roleRepository.delete(role);
    }

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.UPDATE,
            module = SourceModule.AUTH
    )
    @Override
    @Transactional
    public RoleResponse addPermissionsToRole(UUID id, Set<String> permissions) {
        Role role = findRoleById(id);
        if (permissions != null && !permissions.isEmpty()) {
            Set<Permission> newPermissions = permissionRepository.findByNameIn(permissions);
            role.getPermissions().addAll(newPermissions);
            roleRepository.save(role);
        }
        return roleMapper.toRoleResponse(role);
    }

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.UPDATE,
            module = SourceModule.AUTH
    )
    @Override
    @Transactional
    public RoleResponse removePermissionsFromRole(UUID id, Set<String> permissions) {
        Role role = findRoleById(id);
        if (permissions != null && !permissions.isEmpty()) {
            role.getPermissions().removeIf(p -> permissions.contains(p.getName()));
            roleRepository.save(role);
        }
        return roleMapper.toRoleResponse(role);
    }

    @Auditable(
            entityClass = Role.class,
            entityType = "Role",
            action = AuditAction.UPDATE,
            module = SourceModule.AUTH
    )
    @Override
    @Transactional
    public RoleResponse setPermissionsForRole(UUID id, Set<String> permissions) {
        Role role = findRoleById(id);
        Set<Permission> newPermissions = new HashSet<>();
        if (permissions != null && !permissions.isEmpty()) {
            newPermissions = permissionRepository.findByNameIn(permissions);
        }
        role.setPermissions(newPermissions);
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public RoleResponse getRoleById(UUID id) {
        return roleMapper.toRoleResponse(findRoleById(id));
    }

    private Role findRoleById(UUID id) {
        return roleRepository.findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Role", id)
        );
    }
}
