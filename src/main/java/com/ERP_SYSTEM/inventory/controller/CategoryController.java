package com.ERP_SYSTEM.inventory.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.inventory.dto.request.CreateCategoryRequest;
import com.ERP_SYSTEM.inventory.dto.response.CategoryResponse;
import com.ERP_SYSTEM.inventory.service.CategoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories() {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.getById(id)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
            @RequestBody @Valid CreateCategoryRequest request) {
        CategoryResponse created = categoryService.create(request);
        return new ResponseEntity<>(ApiResponse.success(created), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
            @PathVariable UUID id,
            @RequestBody @Valid CreateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.success(categoryService.update(id, request)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable UUID id) {
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }
}