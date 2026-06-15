package com.ERP_SYSTEM.auth.config;

import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Set;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    public void run(String... args) {
        // Tạo permissions trước
        createPermission("USER_CREATE", "Tạo người dùng");
        createPermission("USER_UPDATE", "Sửa người dùng");
        createPermission("USER_DELETE", "Xóa người dùng");
        createPermission("USER_VIEW",   "Xem người dùng");
        createPermission("PRODUCT_CREATE", "Tạo sản phẩm");
        createPermission("PRODUCT_UPDATE", "Sửa sản phẩm");
        createPermission("PRODUCT_DELETE", "Xóa sản phẩm");
        createPermission("PRODUCT_VIEW",   "Xem sản phẩm");
        createPermission("ORDER_CREATE", "Tạo đơn hàng");
        createPermission("ORDER_VIEW",   "Xem đơn hàng");
        createPermission("REPORT_VIEW",  "Xem báo cáo");

        // Tạo roles với permissions tương ứng
        createRole("USER", Set.of(
                "PRODUCT_VIEW", "ORDER_CREATE", "ORDER_VIEW"
        ));
        createRole("MANAGER", Set.of(
                "PRODUCT_VIEW", "PRODUCT_CREATE", "PRODUCT_UPDATE",
                "ORDER_CREATE", "ORDER_VIEW", "REPORT_VIEW"
        ));
        createRole("ADMIN", Set.of(
                "USER_CREATE", "USER_UPDATE", "USER_DELETE", "USER_VIEW",
                "PRODUCT_CREATE", "PRODUCT_UPDATE",
                "PRODUCT_DELETE", "PRODUCT_VIEW",
                "ORDER_CREATE", "ORDER_VIEW", "REPORT_VIEW"
        ));
    }
    private void createPermission(String name, String description) {
        if(permissionRepository.findByName(name).isEmpty()){
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.info("Tạo permission: {}", name);
        }
    }
        private void createRole(String name,  Set<String> permissionName) {
        if(roleRepository.findByName(name).isEmpty()){
            Set<Permission> permissions = permissionRepository.findByNameIn(permissionName);

            Role role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            roleRepository.save(role);
            log.info("Tạo role: {} với {} permission", name, permissions.size());
        }
    }
}
