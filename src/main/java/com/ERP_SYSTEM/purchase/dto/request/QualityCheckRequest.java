package com.ERP_SYSTEM.purchase.dto.request;

import com.ERP_SYSTEM.purchase.enums.QualityCheckStatus;
import jakarta.validation.constraints.NotNull;

public record QualityCheckRequest(
        @NotNull(message = "Kết quả kiểm tra chất lượng không được để trống")
        QualityCheckStatus result,

        String notes
) {
}
