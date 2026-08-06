package com.ERP_SYSTEM.sales.service.Impl;

import com.ERP_SYSTEM.sales.dto.request.*;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderSummaryResponse;
import com.ERP_SYSTEM.sales.mapper.SalesOrderMapper;
import com.ERP_SYSTEM.sales.repository.CustomerRepository;
import com.ERP_SYSTEM.sales.repository.SalesOrderRepository;
import com.ERP_SYSTEM.sales.service.SalesOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderMapper salesOrderMapper;

    @Override
    public SalesOrderDetailResponse create(CreateSalesOrderRequest request) {
        
        return null;
    }

    @Override
    public SalesOrderDetailResponse update(UUID id, UpdateSalesOrderRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse getById(UUID id) {
        return null;
    }

    @Override
    public Page<SalesOrderSummaryResponse> getAll(Pageable pageable) {
        return null;
    }

    @Override
    public Page<SalesOrderSummaryResponse> search(SalesOrderSearchRequest request, Pageable pageable) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse addItem(UUID id, AddSalesOrderItemRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse updateItem(UUID id, UpdateSalesOrderItemRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse removeItem(UUID id, UUID ItemId) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse submitForApproval(UUID id) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse approve(UUID id, ApproveSalesOrderRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse reject(UUID id, RejectSalesOrderRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse confirm(UUID id) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse cancel(UUID id, CancelSalesOrderRequest request) {
        return null;
    }

    @Override
    public SalesOrderDetailResponse close(UUID id) {
        return null;
    }

    @Override
    public void delete(UUID id) {

    }
}
