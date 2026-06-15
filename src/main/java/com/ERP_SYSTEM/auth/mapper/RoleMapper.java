package com.ERP_SYSTEM.auth.mapper;

import com.ERP_SYSTEM.auth.dto.response.PermissionResponse;
import com.ERP_SYSTEM.auth.dto.response.RoleResponse;
import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.entity.Role;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import java.util.Set;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface RoleMapper {

        @Mapping(source = "permissions", target = "permissions",
                qualifiedByName = "permissionsToStrings")
        RoleResponse toRoleResponse(Role role);

        PermissionResponse toPermissionResponse(Permission permission);

        @Named("permissionsToStrings")
        default Set<String> permissionsToStrings(
                Set<Permission> permissions) {
            if (permissions == null) return Set.of();
            return permissions.stream()
                    .map(Permission::getName)
                    .collect(Collectors.toSet());
        }
    }

