package com.ERP_SYSTEM.inventory.service;

import com.ERP_SYSTEM.inventory.dto.request.CreateCategoryRequest;
import com.ERP_SYSTEM.inventory.dto.response.CategoryResponse;

import java.util.List;
import java.util.UUID;

public interface CategoryService {
    CategoryResponse create(CreateCategoryRequest request);

    CategoryResponse update(UUID id, CreateCategoryRequest request);

    CategoryResponse getById(UUID id);

    List<CategoryResponse> getAll();

    void delete(UUID id);
}
