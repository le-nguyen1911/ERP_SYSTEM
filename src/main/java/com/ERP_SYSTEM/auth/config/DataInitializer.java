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


        // USER
        createPermission("USER_CREATE", "Tạo người dùng");
        createPermission("USER_UPDATE", "Cập nhật người dùng");
        createPermission("USER_DELETE", "Xóa người dùng");
        createPermission("USER_VIEW", "Xem người dùng");

        // CATEGORY
        createPermission("CATEGORY_CREATE", "Tạo danh mục");
        createPermission("CATEGORY_UPDATE", "Cập nhật danh mục");
        createPermission("CATEGORY_DELETE", "Xóa danh mục");
        createPermission("CATEGORY_VIEW", "Xem danh mục");

        // UNIT
        createPermission("UNIT_CREATE", "Tạo đơn vị tính");
        createPermission("UNIT_UPDATE", "Cập nhật đơn vị tính");
        createPermission("UNIT_DELETE", "Xóa đơn vị tính");
        createPermission("UNIT_VIEW", "Xem đơn vị tính");

        // WAREHOUSE
        createPermission("WAREHOUSE_CREATE", "Tạo kho");
        createPermission("WAREHOUSE_UPDATE", "Cập nhật kho");
        createPermission("WAREHOUSE_VIEW", "Xem kho");

        // PRODUCT
        createPermission("PRODUCT_CREATE", "Tạo sản phẩm");
        createPermission("PRODUCT_UPDATE", "Cập nhật sản phẩm");
        createPermission("PRODUCT_DELETE", "Ngừng sử dụng sản phẩm");
        createPermission("PRODUCT_VIEW", "Xem sản phẩm");

        // STOCK
        createPermission("STOCK_IMPORT", "Nhập kho");
        createPermission("STOCK_EXPORT", "Xuất kho");
        createPermission("STOCK_TRANSFER", "Chuyển kho");
        createPermission("STOCK_VIEW", "Xem tồn kho");
        createPermission("STOCK_UPDATE", "Cập nhật tồn kho");


        //SUPPLIER
        createPermission("SUPPLIER_CREATE", "Tạo nhà cung cấp");
        createPermission("SUPPLIER_UPDATE", "Cập nhật nhà cung cấp");
        createPermission("SUPPLIER_VIEW", "Xem nhà cung cấp");

        //PURCHASE ORDER
        createPermission("PURCHASE_CREATE", "Tạo đơn đặt hàng");
        createPermission("PURCHASE_UPDATE", "Cập nhật đơn đặt hàng");
        createPermission("PURCHASE_APPROVE", "Duyệt đơn đặt hàng");
        createPermission("PURCHASE_CANCEL", "Hủy đơn đặt hàng");

        //GOODS RECEIPT
        createPermission("GOODS_RECEIPT_CREATE", "Tạo phiếu nhận hàng");
        createPermission("GOODS_RECEIPT_VIEW", "Xem phiếu nhận hàng");
        createPermission("GOODS_RECEIPT_IMPORT", "Nhập kho từ phiếu nhận hàng");

        // CUSTOMER
        createPermission("CUSTOMER_CREATE", "Tạo khách hàng");
        createPermission("CUSTOMER_UPDATE", "Cập nhật khách hàng");
        createPermission("CUSTOMER_VIEW", "Xem khách hàng");

        // SALES ORDER
        createPermission("SALES_CREATE", "Tạo đơn bán hàng");
        createPermission("SALES_UPDATE", "Cập nhật đơn bán hàng");
        createPermission("SALES_APPROVE", "Duyệt đơn bán hàng");
        createPermission("SALES_VIEW", "Xem chi tiết đơn bán hàng");
        createPermission("SALES_CANCEL", "Hủy đơn bán hàng");

        // DELIVERY
        createPermission("DELIVERY_CREATE", "Tạo phiếu giao hàng");
        createPermission("DELIVERY_VIEW", "Xem phiếu giao hàng");
        createPermission("DELIVERY_EXPORT", "Xuất kho từ phiếu giao hàng");

        // REPORT
        createPermission("REPORT_VIEW", "Xem báo cáo");


        // ROLE USER
        createRole("USER", Set.of(
                "CATEGORY_VIEW",
                "UNIT_VIEW",
                "WAREHOUSE_VIEW",
                "PRODUCT_VIEW",
                "STOCK_VIEW"
        ));

        // ROLE MANAGER
        createRole("MANAGER", Set.of(
                "CATEGORY_CREATE",
                "CATEGORY_UPDATE",
                "CATEGORY_VIEW",

                "UNIT_CREATE",
                "UNIT_UPDATE",
                "UNIT_VIEW",

                "WAREHOUSE_CREATE",
                "WAREHOUSE_UPDATE",
                "WAREHOUSE_VIEW",

                "PRODUCT_CREATE",
                "PRODUCT_UPDATE",
                "PRODUCT_VIEW",

                "STOCK_IMPORT",
                "STOCK_EXPORT",
                "STOCK_TRANSFER",
                "STOCK_VIEW",
                "STOCK_UPDATE",
                "REPORT_VIEW"
        ));

        // ROLE ADMIN
        createRole("ADMIN", Set.of(
                "USER_CREATE",
                "USER_UPDATE",
                "USER_DELETE",
                "USER_VIEW",

                "CATEGORY_CREATE",
                "CATEGORY_UPDATE",
                "CATEGORY_DELETE",
                "CATEGORY_VIEW",

                "UNIT_CREATE",
                "UNIT_UPDATE",
                "UNIT_DELETE",
                "UNIT_VIEW",

                "WAREHOUSE_CREATE",
                "WAREHOUSE_UPDATE",
                "WAREHOUSE_VIEW",

                "PRODUCT_CREATE",
                "PRODUCT_UPDATE",
                "PRODUCT_DELETE",
                "PRODUCT_VIEW",

                "STOCK_IMPORT",
                "STOCK_EXPORT",
                "STOCK_TRANSFER",
                "STOCK_VIEW",
                "STOCK_UPDATE",

                "SUPPLIER_CREATE",
                "SUPPLIER_UPDATE",
                "SUPPLIER_VIEW",

                "PURCHASE_CREATE",
                "PURCHASE_UPDATE",
                "PURCHASE_APPROVE",
                "PURCHASE_CANCEL",

                "GOODS_RECEIPT_CREATE",
                "GOODS_RECEIPT_VIEW",
                "GOODS_RECEIPT_IMPORT",

                "CUSTOMER_CREATE",
                "CUSTOMER_UPDATE",
                "CUSTOMER_VIEW",

                "SALES_CREATE",
                "SALES_UPDATE",
                "SALES_VIEW",
                "SALES_APPROVE",
                "SALES_CANCEL",

                "DELIVERY_CREATE",
                "DELIVERY_VIEW",
                "DELIVERY_EXPORT",

                "REPORT_VIEW"
        ));
    }

    private void createPermission(String name, String description) {
        if (permissionRepository.findByName(name).isEmpty()) {
            Permission permission = new Permission();
            permission.setName(name);
            permission.setDescription(description);
            permissionRepository.save(permission);
            log.info("Tạo permission: {}", name);
        }
    }

    private void createRole(String name, Set<String> permissionName) {
        if (roleRepository.findByName(name).isEmpty()) {
            Set<Permission> permissions = permissionRepository.findByNameIn(permissionName);

            Role role = new Role();
            role.setName(name);
            role.setPermissions(permissions);
            roleRepository.save(role);
            log.info("Tạo role: {} với {} permission", name, permissions.size());
        }
    }
}
