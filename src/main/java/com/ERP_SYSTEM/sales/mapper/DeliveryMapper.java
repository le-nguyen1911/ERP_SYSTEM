package com.ERP_SYSTEM.sales.mapper;

import com.ERP_SYSTEM.sales.dto.response.DeliveryDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.DeliveryItemResponse;
import com.ERP_SYSTEM.sales.dto.response.DeliverySummaryResponse;
import com.ERP_SYSTEM.sales.entity.Delivery;
import com.ERP_SYSTEM.sales.entity.DeliveryItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {CustomerMapper.class}
)
public interface DeliveryMapper {

    @Mapping(target = "productName", source = "salesOrderItem.productName")
    DeliveryItemResponse toItemResponse(DeliveryItem item);

    List<DeliveryItemResponse> toItemResponseList(List<DeliveryItem> items);

    @Mapping(target = "soNumber", source = "salesOrder.soNumber")
    @Mapping(target = "customerName", source = "customer.customerName")
    DeliverySummaryResponse toSummaryResponse(Delivery delivery);

    List<DeliverySummaryResponse> toSummaryResponseList(List<Delivery> deliveries);

    @Mapping(target = "salesOrderId", source = "salesOrder.id")
    @Mapping(target = "soNumber", source = "salesOrder.soNumber")
    DeliveryDetailResponse toDetailResponse(Delivery delivery);
}