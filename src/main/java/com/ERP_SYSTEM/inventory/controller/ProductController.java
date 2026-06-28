package com.ERP_SYSTEM.inventory.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.inventory.dto.request.CreateProductRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateProductRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductResponse;
import com.ERP_SYSTEM.inventory.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {
    private final ProductService productService;


    @GetMapping
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.getAll(pageable)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<Page<ProductResponse>>> getSearchProducts(String search, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(productService.search(search, pageable)));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(productService.getById(id)));
    }

    @GetMapping("/code/{code}")
    @PreAuthorize("hasAuthority('PRODUCT_VIEW')")
    public ResponseEntity<ApiResponse<ProductResponse>> getProductByCode(@PathVariable String code) {
        return ResponseEntity.ok(ApiResponse.success(productService.getByCode(code)));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('PRODUCT_CREATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@Valid @RequestBody CreateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Tạo sản phẩm thành công", productService.create(request)));
    }

    @PutMapping
    @PreAuthorize("hasAuthority('PRODUCT_UPDATE')")
    public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@PathVariable UUID id, @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật sản phẩm thành công", productService.update(id, request)));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('PRODUCT_DELETE')")
    public ResponseEntity<ApiResponse<ProductResponse>> deactivateProduct(@PathVariable UUID id) {
        productService.deactivate(id);
        return ResponseEntity.ok(ApiResponse.success("Vô hiệu hóa sản phẩm", null));
    }
}
