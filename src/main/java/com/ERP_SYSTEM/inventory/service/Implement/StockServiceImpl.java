package com.ERP_SYSTEM.inventory.service.Implement;

import com.ERP_SYSTEM.inventory.dto.request.StockTransactionRequest;
import com.ERP_SYSTEM.inventory.dto.request.StockTransferRequest;
import com.ERP_SYSTEM.inventory.dto.request.UpdateMinQuantityRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.dto.response.StockTransactionResponse;
import com.ERP_SYSTEM.inventory.entity.Product;
import com.ERP_SYSTEM.inventory.entity.ProductStock;
import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import com.ERP_SYSTEM.inventory.entity.StockTransaction.TransactionType;
import com.ERP_SYSTEM.inventory.entity.Warehouse;
import com.ERP_SYSTEM.inventory.mapper.StockMapper;
import com.ERP_SYSTEM.inventory.repository.ProductRepository;
import com.ERP_SYSTEM.inventory.repository.ProductStockRepository;
import com.ERP_SYSTEM.inventory.repository.StockTransactionRepository;
import com.ERP_SYSTEM.inventory.repository.WarehouseRepository;
import com.ERP_SYSTEM.inventory.service.StockService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StockServiceImpl implements StockService {
    private final ProductStockRepository productStockRepository;
    private final StockTransactionRepository stockTransactionRepository;
    private final ProductRepository productRepository;
    private final WarehouseRepository warehouseRepository;
    private final StockMapper stockMapper;

    @Override
    @Transactional
    public StockTransactionResponse processTransaction(StockTransactionRequest request) {
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy sản phẩm"));

        if (!product.getActive()) {
            throw new RuntimeException("Sản phẩm đã ngừng kinh doanh");

        }
        Warehouse warehouse = warehouseRepository.findById(request.warehouseId())
                .orElseThrow(() -> new RuntimeException("kHÔNG TÌM THẤY Kho"));

        if (!warehouse.getActive()) {
            throw new RuntimeException("Kho đã" + warehouse.getName() + "ngừng hoạt động ");
        }
        ProductStock stock = productStockRepository.findByProductIdAndWarehouseId(product.getId(), warehouse.getId())
                .orElseGet(() -> {
                    ProductStock newStock = ProductStock.builder()
                            .product(product)
                            .warehouse(warehouse)
                            .quantity(0)
                            .minQuantity(0)
                            .build();
                    return productStockRepository.save(newStock);
                });

        if (request.type() == StockTransaction.TransactionType.IMPORT) {
            stock.setQuantity(stock.getQuantity() + request.quantity());
        } else {
            if (stock.getQuantity() < request.quantity()) {
                throw new RuntimeException(
                        String.format(
                                "Tồn kho không đủ. " +
                                        "Sản phẩm: %s, Kho: %s, " +
                                        "Hiện có: %d, Cần xuất: %d",
                                product.getName(),
                                warehouse.getName(),
                                stock.getQuantity(),
                                request.quantity()
                        )
                );
            }
            stock.setQuantity(stock.getQuantity() - request.quantity());
        }
        productStockRepository.save(stock);

        String currentUser = SecurityContextHolder.getContext().getAuthentication().getName();

        StockTransaction transaction = StockTransaction.builder()
                .product(product)
                .warehouse(warehouse)
                .type(request.type())
                .quantity(request.quantity())
                .unitPrice(request.unitPrice())
                .note(request.note())
                .createdBy(currentUser)
                .build();
        stockTransactionRepository.save(transaction);

        return stockMapper.toStockTransactionResponse(transaction);
    }

    @Override
    @Transactional
    public List<StockTransactionResponse> transferStock(StockTransferRequest request) {
        if (request.fromWarehouseId().equals(request.toWarehouseId())) {
            throw new RuntimeException("Kho nguòn và kho đích không khớp");
        }

        String transferRef = "TRF-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        StockTransactionRequest exportRequest = new StockTransactionRequest(
                request.productId(),
                request.fromWarehouseId(),
                TransactionType.EXPORT,
                request.quantity(),
                null,
                String.format("[%s] Điều chuyển đến kho #%s. %s",
                        transferRef, request.toWarehouseId(),
                        request.note() != null ? request.note() : "")
        );
        StockTransactionResponse exportResult = processTransaction(exportRequest);


        StockTransactionRequest importRequest = new StockTransactionRequest(
                request.productId(),
                request.toWarehouseId(),
                TransactionType.IMPORT,
                request.quantity(),
                null,
                String.format("[%s] Điều chuyển từ kho #%s. %s",
                        transferRef, request.fromWarehouseId(),
                        request.note() != null ? request.note() : "")
        );
        StockTransactionResponse importResult = processTransaction(importRequest);

        return List.of(exportResult, importResult);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductStockResponse> getStockByProduct(UUID productId) {
        return productStockRepository.findByProductId(productId)
                .stream()
                .map(stockMapper::toProductStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductStockResponse> getStockByWarehouse(UUID warehouseId) {
        return productStockRepository.findByWarehouseId(warehouseId)
                .stream()
                .map(stockMapper::toProductStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public List<ProductStockResponse> getLowStockAlerts() {
        return productStockRepository.findLowStockProducts()
                .stream()
                .map(stockMapper::toProductStockResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    @Override
    public Page<StockTransactionResponse> getTransactionHistory(UUID productId, Pageable pageable) {
        return stockTransactionRepository.findByProductId(productId, pageable).map(stockMapper::toStockTransactionResponse);
    }

    @Transactional
    @Override
    public ProductStockResponse updateMinQuantity(UpdateMinQuantityRequest request) {
        ProductStock stock = productStockRepository.
                findByProductIdAndWarehouseId(request.productId(), request.warehouseId())
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu tồn kho"));
        stock.setMinQuantity(request.minQuantity());
        productStockRepository.save(stock);
        return stockMapper.toProductStockResponse(stock);
    }
}
