package com.ERP_SYSTEM.purchase.mapper;

import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptItemResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptSummaryResponse;
import com.ERP_SYSTEM.purchase.entity.GoodsReceipt;
import com.ERP_SYSTEM.purchase.entity.GoodsReceiptItem;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(
        componentModel = "spring",
        uses = {SupplierMapper.class}
)
public interface GoodsReceiptMapper {

    GoodsReceiptItemResponse toItemResponse(GoodsReceiptItem item);

    List<GoodsReceiptItemResponse> toItemResponseList(List<GoodsReceiptItem> items);


    GoodsReceiptSummaryResponse toSummaryResponse(GoodsReceipt goodsReceipt);

    List<GoodsReceiptSummaryResponse> toSummaryResponseList(List<GoodsReceipt> goodsReceipts);

    GoodsReceiptDetailResponse toDetailResponse(GoodsReceipt goodsReceipt);
}
