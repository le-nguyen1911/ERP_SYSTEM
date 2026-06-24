package com.ERP_SYSTEM.inventory.service;


import com.ERP_SYSTEM.inventory.dto.request.StockTransactionRequest;
import com.ERP_SYSTEM.inventory.dto.request.StockTransferRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateMinQuantityRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.dto.response.StockTransactionResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.UUID;

public interface StockService {

    StockTransactionResponse processTransaction(
            StockTransactionRequest request);

    List<StockTransactionResponse> transferStock(
            StockTransferRequest request);

    List<ProductStockResponse> getStockByProduct(
            UUID productId);

    List<ProductStockResponse> getStockByWarehouse(
            UUID warehouseId);

    List<ProductStockResponse> getLowStockAlerts();

    Page<StockTransactionResponse> getTransactionHistory(
            UUID productId, Pageable pageable);

    ProductStockResponse updateMinQuantity(
            UpdateMinQuantityRequest request);
}