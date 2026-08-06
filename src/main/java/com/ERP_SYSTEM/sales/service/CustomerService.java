package com.ERP_SYSTEM.sales.service;

import com.ERP_SYSTEM.sales.dto.request.CreateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.request.CustomerSearchRequest;
import com.ERP_SYSTEM.sales.dto.request.UpdateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.response.CustomerResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface CustomerService {
    CustomerResponse create(CreateCustomerRequest request);

    CustomerResponse update(UUID id, UpdateCustomerRequest request);

    CustomerResponse findById(UUID id);

    Page<CustomerResponse> getAll(Pageable pageable);

    Page<CustomerResponse> search(CustomerSearchRequest request, Pageable pageable);

    void delete(UUID id);
}
