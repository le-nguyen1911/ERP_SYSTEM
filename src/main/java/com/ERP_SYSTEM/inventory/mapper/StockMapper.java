package com.ERP_SYSTEM.inventory.mapper;

import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.dto.response.StockTransactionResponse;
import com.ERP_SYSTEM.inventory.dto.response.WarehouseResponse;
import com.ERP_SYSTEM.inventory.entity.ProductStock;
import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import com.ERP_SYSTEM.inventory.entity.Warehouse;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(
        componentModel = "spring",
        uses = {ProductMapper.class}
)
public interface StockMapper {
    WarehouseResponse toWarehouseResponse(Warehouse warehouse);

    @Mapping(
            target = "lowStock",
            expression = "java(stock.getQuantity() <= stock.getMinQuantity() && stock.getMinQuantity() > 0)"
    )
    ProductStockResponse toProductStockResponse(ProductStock stock);

    StockTransactionResponse toStockTransactionResponse(StockTransaction transaction);
}
