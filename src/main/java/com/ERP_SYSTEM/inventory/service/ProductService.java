package com.ERP_SYSTEM.inventory.service;

import com.ERP_SYSTEM.inventory.dto.request.CreateProductRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateProductRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface ProductService {
    
    ProductResponse create(CreateProductRequest request);

    ProductResponse update(UUID id, UpdateProductRequest request);

    ProductResponse getById(UUID id);

    ProductResponse getByCode(String code);

    Page<ProductResponse> getAll(Pageable pageable);


    Page<ProductResponse> search(String search, Pageable pageable);

    ProductResponse deactivate(UUID id);
}
