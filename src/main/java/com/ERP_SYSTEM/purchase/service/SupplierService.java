package com.ERP_SYSTEM.purchase.service;

import com.ERP_SYSTEM.purchase.dto.request.CreateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.request.SupplierSearchRequest;
import com.ERP_SYSTEM.purchase.dto.request.UpdateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.response.SupplierResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SupplierService {
    SupplierResponse create(CreateSupplierRequest request);

    SupplierResponse update(UUID id, UpdateSupplierRequest request);

    SupplierResponse getById(UUID id);

    Page<SupplierResponse> getAll(Pageable pageable);

    Page<SupplierResponse> search(SupplierSearchRequest searchRequest, Pageable pageable);

    void delete(UUID id);
}
