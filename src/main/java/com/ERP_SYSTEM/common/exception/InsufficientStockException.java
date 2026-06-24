package com.ERP_SYSTEM.common.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String productName,
                                      String warehouseName,
                                      int available,
                                      int requested) {
        super(String.format(
                "Tồn kho không đủ. " +
                        "Sản phẩm: %s, " +
                        "Kho: %s, " +
                        "Hiện có: %d, " +
                        "Cần xuất: %d",
                productName, warehouseName,
                available, requested
        ));
    }
}
