package com.ERP_SYSTEM.purchase.mapper;

import com.ERP_SYSTEM.purchase.dto.request.AddPurchaseOrderItemRequest;
import com.ERP_SYSTEM.purchase.dto.request.PurchaseOrderItemRequest;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderItemResponse;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderSummaryResponse;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrder;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {SupplierMapper.class}
)
public interface PurchaseOrderMapper {


    PurchaseOrderItem toItemEntity(PurchaseOrderItemRequest request);

    PurchaseOrderItem toItemEntity(AddPurchaseOrderItemRequest request);

    PurchaseOrderItemResponse toItemResponse(PurchaseOrderItem item);

    List<PurchaseOrderItemResponse> toItemResponseList(List<PurchaseOrderItem> items);


    @Mapping(target = "supplierId", source = "supplier.id")
    @Mapping(target = "supplierName", source = "supplier.supplierName")
    PurchaseOrderSummaryResponse toSummaryResponse(PurchaseOrder purchaseOrder);

    List<PurchaseOrderSummaryResponse> toSummaryResponseList(List<PurchaseOrder> purchaseOrders);

    PurchaseOrderDetailResponse toDetailResponse(PurchaseOrder purchaseOrder);
}