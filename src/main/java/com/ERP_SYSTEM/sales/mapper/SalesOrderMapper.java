package com.ERP_SYSTEM.sales.mapper;

import com.ERP_SYSTEM.sales.dto.request.AddSalesOrderItemRequest;
import com.ERP_SYSTEM.sales.dto.request.SalesOrderItemRequest;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderItemResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderSummaryResponse;
import com.ERP_SYSTEM.sales.entity.SalesOrder;
import com.ERP_SYSTEM.sales.entity.SalesOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CustomerMapper.class}
)
public interface SalesOrderMapper {


    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "lineNumber", ignore = true)
    @Mapping(target = "deliveredQuantity", ignore = true)
    @Mapping(target = "status", ignore = true)
    SalesOrderItem toItemEntity(SalesOrderItemRequest request);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "salesOrder", ignore = true)
    @Mapping(target = "lineNumber", ignore = true)
    @Mapping(target = "deliveredQuantity", ignore = true)
    @Mapping(target = "status", ignore = true)
    SalesOrderItem toItemEntity(AddSalesOrderItemRequest request);

    SalesOrderItemResponse toItemResponse(SalesOrderItem item);

    List<SalesOrderItemResponse> toItemResponseList(List<SalesOrderItem> items);


    @Mapping(target = "customerId", source = "customer.id")
    @Mapping(target = "customerName", source = "customer.customerName")
    SalesOrderSummaryResponse toSummaryResponse(SalesOrder salesOrder);

    List<SalesOrderSummaryResponse> toSummaryResponseList(List<SalesOrder> salesOrders);

    SalesOrderDetailResponse toDetailResponse(SalesOrder salesOrder);
}