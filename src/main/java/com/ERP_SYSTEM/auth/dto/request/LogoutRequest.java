package com.ERP_SYSTEM.auth.dto.request;

import lombok.Data;

@Data
public class LogoutRequest {
    private String refreshToken;
}
