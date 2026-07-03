package com.ERP_SYSTEM.purchase.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;

@Getter
@RequiredArgsConstructor
public enum SupplierRating {
    A_PLUS("A+", "Xuất sắc (95-100%)",
            new BigDecimal("95"), new BigDecimal("100")),

    A("A", "Rất tốt (80-94%)",
            new BigDecimal("80"), new BigDecimal("94")),

    B("B", "Tốt (60-79%)",
            new BigDecimal("60"), new BigDecimal("79")),


    C("C", "Kém (< 60%)",
            new BigDecimal("0"), new BigDecimal("60"));


    private final String code;
    private final String description;
    private final BigDecimal minPercentage;
    private final BigDecimal maxPercentage;

    public static SupplierRating fromPercentage(BigDecimal onTimePercentage) {
        if (onTimePercentage == null || onTimePercentage.compareTo(BigDecimal.ZERO) < 0) {
            return C;
        }

        if (onTimePercentage.compareTo(new BigDecimal("95")) >= 0) {
            return A_PLUS;
        } else if (onTimePercentage.compareTo(new BigDecimal("80")) >= 0) {
            return A;
        } else if (onTimePercentage.compareTo(new BigDecimal("60")) >= 0) {
            return B;
        } else {
            return C;
        }
    }

}
