package com.ERP_SYSTEM.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActiveSessionResponse {
    private Long id;

    private String deviceInfo;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private Boolean current;

}
