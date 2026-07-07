package com.ERP_SYSTEM.purchase.mapper;

import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptItemResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptSummaryResponse;
import com.ERP_SYSTEM.purchase.entity.GoodsReceipt;
import com.ERP_SYSTEM.purchase.entity.GoodsReceiptItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {SupplierMapper.class}
)
public interface GoodsReceiptMapper {
   
    @Mapping(target = "productName", source = "purchaseOrderItem.productName")
    GoodsReceiptItemResponse toItemResponse(GoodsReceiptItem item);

    List<GoodsReceiptItemResponse> toItemResponseList(List<GoodsReceiptItem> items);

    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    @Mapping(target = "supplierName", source = "supplier.supplierName")
    GoodsReceiptSummaryResponse toSummaryResponse(GoodsReceipt goodsReceipt);

    List<GoodsReceiptSummaryResponse> toSummaryResponseList(List<GoodsReceipt> goodsReceipts);

    @Mapping(target = "purchaseOrderId", source = "purchaseOrder.id")
    @Mapping(target = "poNumber", source = "purchaseOrder.poNumber")
    GoodsReceiptDetailResponse toDetailResponse(GoodsReceipt goodsReceipt);
}
