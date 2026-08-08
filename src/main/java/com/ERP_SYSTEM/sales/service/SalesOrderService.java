package com.ERP_SYSTEM.sales.service;

import com.ERP_SYSTEM.sales.dto.request.*;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderSummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface SalesOrderService {
    SalesOrderDetailResponse create(CreateSalesOrderRequest request);

    SalesOrderDetailResponse update(UUID id, UpdateSalesOrderRequest request);

    SalesOrderDetailResponse getById(UUID id);

    Page<SalesOrderSummaryResponse> getAll(Pageable pageable);

    Page<SalesOrderSummaryResponse> search(SalesOrderSearchRequest request, Pageable pageable);

    SalesOrderDetailResponse addItem(UUID soId, AddSalesOrderItemRequest request);

    SalesOrderDetailResponse updateItem(UUID soId, UUID itemId, UpdateSalesOrderItemRequest request);

    SalesOrderDetailResponse removeItem(UUID soId, UUID itemId);

    SalesOrderDetailResponse submitForApproval(UUID id);

    SalesOrderDetailResponse approve(UUID id, ApproveSalesOrderRequest request);

    SalesOrderDetailResponse reject(UUID id, RejectSalesOrderRequest request);

    SalesOrderDetailResponse confirm(UUID id);

    SalesOrderDetailResponse cancel(UUID id, CancelSalesOrderRequest request);

    SalesOrderDetailResponse close(UUID id);

    void delete(UUID id);
}
