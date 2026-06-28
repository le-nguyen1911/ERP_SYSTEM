package com.ERP_SYSTEM.inventory.controller;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.inventory.dto.request.StockTransactionRequest;
import com.ERP_SYSTEM.inventory.dto.request.StockTransferRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateMinQuantityRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.dto.response.StockTransactionResponse;
import com.ERP_SYSTEM.inventory.service.StockService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/stocks")
@RequiredArgsConstructor
public class StockController {
    private final StockService stockService;

    @GetMapping("/product/{id}")
    @PreAuthorize("hasAuthority('STOCK_VIEW')")
    public ResponseEntity<ApiResponse<List<ProductStockResponse>>> getStockByProductId(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getStockByProduct(id)));
    }

    @GetMapping("/warehouse/{id}")
    @PreAuthorize("hasAuthority('STOCK_VIEW')")
    public ResponseEntity<ApiResponse<List<ProductStockResponse>>> getStockByWarehouseId(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getStockByWarehouse(id)));
    }

    @GetMapping("/low-stock")
    @PreAuthorize("hasAuthority('STOCK_VIEW')")
    public ResponseEntity<ApiResponse<List<ProductStockResponse>>> getStockByLowStock() {
        return ResponseEntity.ok(ApiResponse.success(stockService.getLowStockAlerts()));
    }

    @GetMapping("/history/{id}")
    @PreAuthorize("hasAuthority('STOCK_VIEW')")
    public ResponseEntity<ApiResponse<Page<StockTransactionResponse>>> getTransactionHistory(@PathVariable UUID id, Pageable pageable) {
        return ResponseEntity.ok(ApiResponse.success(stockService.getTransactionHistory(id, pageable)));
    }

    @PostMapping("/transaction")
    @PreAuthorize("hasAuthority('STOCK_IMPORT') " + "or hasAuthority('STOCK_EXPORT')")
    public ResponseEntity<ApiResponse<StockTransactionResponse>> process(
            @Valid @RequestBody StockTransactionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Giao dịch thành công",
                        stockService.processTransaction(request))
        );
    }

    @PostMapping("/transfer")
    @PreAuthorize("hasAuthority('STOCK_TRANSFER')")
    public ResponseEntity<ApiResponse<List<StockTransactionResponse>>> transfer(
            @Valid @RequestBody StockTransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.success("Điều chuyển thành công",
                        stockService.transferStock(request))
        );
    }

    @PutMapping
    @PreAuthorize("hasAuthority('STOCK_UPDATE')")
    public ResponseEntity<ApiResponse<ProductStockResponse>> updateStock(@Valid @RequestBody UpdateMinQuantityRequest request) {
        return ResponseEntity.ok(ApiResponse.success("Cập nhật tòn kho thành công", stockService.updateMinQuantity(request)));
    }


}
