package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.auth.dto.request.CreateRoleRequest;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;
import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.mapper.RoleMapper;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import com.ERP_SYSTEM.auth.service.RoleService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class RoleServiceImpl implements RoleService {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;
    private final RoleMapper roleMapper;


    @Override
    @Transactional
    public RoleResponse createRole(CreateRoleRequest request) {
        if (roleRepository.findByName(request.getName()).isPresent()) {
            throw new RuntimeException("Role đã tồn tại" + request.getName());
        }

        Set<Permission> permissions = new HashSet<>();
        if (request.getPermissions() != null && !request.getPermissions().isEmpty()) {
            permissions = permissionRepository.findByNameIn(request.getPermissions());
        }
        Role role = Role.builder()
                .name(request.getName())
                .description(request.getDescription())
                .permissions(permissions)
                .build();
        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse addPermissionsToRole(Long id, Set<String> permissions) {
        Role role = findRoleById(id);
        Set<Permission> newPermissions = permissionRepository.findByNameIn(permissions);
        role.getPermissions().addAll(newPermissions);
        roleRepository.save(role);

        return roleMapper.toRoleResponse(role);
    }

    @Override
    @Transactional
    public RoleResponse removePermissionsFromRole(Long id, Set<String> permissions) {
        Role role = findRoleById(id);
        Set<Permission> Permissions = permissionRepository.findByNameIn(permissions);

        role.getPermissions().removeIf(p -> permissions.contains(p.getName()));

        roleRepository.save(role);
        return roleMapper.toRoleResponse(role);
    }

    @Override
    public List<RoleResponse> getAllRoles() {
        return roleRepository.findAll()
                .stream()
                .map(roleMapper::toRoleResponse)
                .collect(Collectors.toList());
    }

    @Override
    public RoleResponse getRoleById(Long id) {
        return roleMapper.toRoleResponse(findRoleById(id));
    }

    private Role findRoleById(Long id) {
        return roleRepository.findById(id).orElseThrow(
                ()-> new RuntimeException("Không tìm thấy role với id" + id)
        );
    }
}
