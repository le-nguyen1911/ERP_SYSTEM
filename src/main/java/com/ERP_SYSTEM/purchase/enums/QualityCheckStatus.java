package com.ERP_SYSTEM.purchase.enums;


import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QualityCheckStatus {

    PENDING("Chờ kiểm tra", "Chưa kiểm tra chất lượng"),
    PASSED("Đạt", "Kiểm tra chất lượng Được phê duyệt"),
    FAILED("Không đạt", "Kiểm tra chất lượng bị từ chối");


    private final String displayName;
    private final String description;

}
