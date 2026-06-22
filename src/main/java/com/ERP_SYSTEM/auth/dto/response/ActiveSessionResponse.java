package com.ERP_SYSTEM.auth.dto.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class ActiveSessionResponse {
    private UUID id;

    private String deviceInfo;

    private LocalDateTime createdAt;

    private LocalDateTime expiresAt;

    private Boolean current;

}
