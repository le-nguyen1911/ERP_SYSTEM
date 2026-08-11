package com.ERP_SYSTEM.sales.service;

import com.ERP_SYSTEM.sales.dto.request.CreateDeliveryRequest;
import com.ERP_SYSTEM.sales.dto.response.DeliveryDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.DeliverySummaryResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeliveryService {
    DeliveryDetailResponse create(CreateDeliveryRequest request);

    DeliveryDetailResponse getById(UUID id);

    Page<DeliverySummaryResponse> getAll(Pageable pageable);

    Page<DeliverySummaryResponse> getBySalesOrder(UUID salesOrderId, Pageable pageable);

    DeliveryDetailResponse retryInventoryExport(UUID id);

    void retryAllFailedInventoryExports();

    DeliveryDetailResponse markAsDelivered(UUID id);

    void cancel(UUID id, String reason);
}
