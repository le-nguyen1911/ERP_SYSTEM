package com.ERP_SYSTEM.auth.config;

import com.ERP_SYSTEM.auth.entity.Permission;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.repository.PermissionRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component
@Slf4j
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    @Override
    @Transactional
    public void run(String... args) {
        // 1. Định nghĩa danh sách tất cả các Permission cần có
        Map<String, String> permissionDefinitions = getPermissionDefinitions();

        // 2. Query DB 1 LẦN DUY NHẤT để lấy tất cả Permission đang có
        Map<String, Permission> existingPermissionMap = permissionRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Permission::getName, Function.identity()));

        // 3. Lọc ra các Permission chưa tồn tại trong DB
        List<Permission> newPermissionsToSave = new ArrayList<>();
        for (Map.Entry<String, String> entry : permissionDefinitions.entrySet()) {
            String name = entry.getKey();
            String description = entry.getValue();

            if (!existingPermissionMap.containsKey(name)) {
                Permission p = new Permission();
                p.setName(name);
                p.setDescription(description);
                newPermissionsToSave.add(p);
            }
        }

        // 4. Batch Save các Permission mới và cập nhật Map tra cứu
        if (!newPermissionsToSave.isEmpty()) {
            List<Permission> savedPermissions = permissionRepository.saveAll(newPermissionsToSave);
            savedPermissions.forEach(p -> existingPermissionMap.put(p.getName(), p));
            log.info("Khởi tạo thành công {} permissions mới vào database", savedPermissions.size());
        }

        // 5. Khởi tạo / Cập nhật các Role
        initRole("USER", Set.of(
                "CATEGORY_VIEW", "UNIT_VIEW", "WAREHOUSE_VIEW", "PRODUCT_VIEW", "STOCK_VIEW"
        ), existingPermissionMap);

        initRole("MANAGER", Set.of(
                "CATEGORY_CREATE", "CATEGORY_UPDATE", "CATEGORY_VIEW",
                "UNIT_CREATE", "UNIT_UPDATE", "UNIT_VIEW",
                "WAREHOUSE_CREATE", "WAREHOUSE_UPDATE", "WAREHOUSE_VIEW",
                "PRODUCT_CREATE", "PRODUCT_UPDATE", "PRODUCT_VIEW",
                "STOCK_IMPORT", "STOCK_EXPORT", "STOCK_TRANSFER", "STOCK_VIEW", "STOCK_UPDATE",
                "REPORT_VIEW"
        ), existingPermissionMap);

        initRole("ADMIN", permissionDefinitions.keySet(), existingPermissionMap);
    }

    private void initRole(String roleName, Set<String> permissionNames, Map<String, Permission> permissionMap) {
        if (permissionNames == null || permissionNames.isEmpty()) {
            return;
        }

        // Tra cứu Permission từ Map RAM thay vì query lại DB
        Set<Permission> targetPermissions = permissionNames.stream()
                .map(permissionMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        Role role = roleRepository.findByName(roleName).orElseGet(() -> {
            Role newRole = new Role();
            newRole.setName(roleName);
            newRole.setPermissions(new HashSet<>());
            return newRole;
        });

        if (role.getPermissions() == null) {
            role.setPermissions(new HashSet<>());
        }

        int previousSize = role.getPermissions().size();
        role.getPermissions().addAll(targetPermissions);
        int addedCount = role.getPermissions().size() - previousSize;

        if (role.getId() == null) {
            roleRepository.save(role);
            log.info("Tạo mới role: {} với {} permission", roleName, targetPermissions.size());
        } else if (addedCount > 0) {
            roleRepository.save(role);
            log.info("Cập nhật bổ sung {} permission mới cho role: {}", addedCount, roleName);
        } else {
            log.debug("Role: {} đã có đủ các permission, không cần cập nhật", roleName);
        }
    }

    private Map<String, String> getPermissionDefinitions() {
        Map<String, String> map = new LinkedHashMap<>();
        // USER
        map.put("USER_CREATE", "Tạo người dùng");
        map.put("USER_UPDATE", "Cập nhật người dùng");
        map.put("USER_DELETE", "Xóa người dùng");
        map.put("USER_VIEW", "Xem người dùng");

        // CATEGORY
        map.put("CATEGORY_CREATE", "Tạo danh mục");
        map.put("CATEGORY_UPDATE", "Cập nhật danh mục");
        map.put("CATEGORY_DELETE", "Xóa danh mục");
        map.put("CATEGORY_VIEW", "Xem danh mục");

        // UNIT
        map.put("UNIT_CREATE", "Tạo đơn vị tính");
        map.put("UNIT_UPDATE", "Cập nhật đơn vị tính");
        map.put("UNIT_DELETE", "Xóa đơn vị tính");
        map.put("UNIT_VIEW", "Xem đơn vị tính");

        // WAREHOUSE
        map.put("WAREHOUSE_CREATE", "Tạo kho");
        map.put("WAREHOUSE_UPDATE", "Cập nhật kho");
        map.put("WAREHOUSE_VIEW", "Xem kho");
        map.put("WAREHOUSE_DELETE", "Xoá kho");

        // PRODUCT
        map.put("PRODUCT_CREATE", "Tạo sản phẩm");
        map.put("PRODUCT_UPDATE", "Cập nhật sản phẩm");
        map.put("PRODUCT_DELETE", "Ngừng sử dụng sản phẩm");
        map.put("PRODUCT_VIEW", "Xem sản phẩm");

        // STOCK
        map.put("STOCK_IMPORT", "Nhập kho");
        map.put("STOCK_EXPORT", "Xuất kho");
        map.put("STOCK_TRANSFER", "Chuyển kho");
        map.put("STOCK_VIEW", "Xem tồn kho");
        map.put("STOCK_UPDATE", "Cập nhật tồn kho");

        // SUPPLIER
        map.put("SUPPLIER_CREATE", "Tạo nhà cung cấp");
        map.put("SUPPLIER_UPDATE", "Cập nhật nhà cung cấp");
        map.put("SUPPLIER_VIEW", "Xem nhà cung cấp");

        // PURCHASE ORDER
        map.put("PURCHASE_CREATE", "Tạo đơn đặt hàng");
        map.put("PURCHASE_UPDATE", "Cập nhật đơn đặt hàng");
        map.put("PURCHASE_APPROVE", "Duyệt đơn đặt hàng");
        map.put("PURCHASE_CANCEL", "Hủy đơn đặt hàng");

        // GOODS RECEIPT
        map.put("GOODS_RECEIPT_CREATE", "Tạo phiếu nhận hàng");
        map.put("GOODS_RECEIPT_VIEW", "Xem phiếu nhận hàng");
        map.put("GOODS_RECEIPT_IMPORT", "Nhập kho từ phiếu nhận hàng");

        // CUSTOMER
        map.put("CUSTOMER_CREATE", "Tạo khách hàng");
        map.put("CUSTOMER_UPDATE", "Cập nhật khách hàng");
        map.put("CUSTOMER_VIEW", "Xem khách hàng");

        // SALES ORDER
        map.put("SALES_CREATE", "Tạo đơn bán hàng");
        map.put("SALES_UPDATE", "Cập nhật đơn bán hàng");
        map.put("SALES_APPROVE", "Duyệt đơn bán hàng");
        map.put("SALES_VIEW", "Xem chi tiết đơn bán hàng");
        map.put("SALES_CANCEL", "Hủy đơn bán hàng");

        // DELIVERY
        map.put("DELIVERY_CREATE", "Tạo phiếu giao hàng");
        map.put("DELIVERY_VIEW", "Xem phiếu giao hàng");
        map.put("DELIVERY_EXPORT", "Xuất kho từ phiếu giao hàng");

        // REPORT
        map.put("REPORT_VIEW", "Xem báo cáo");

        return map;
    }
}