package com.ERP_SYSTEM.auth.dto.response;

import lombok.Data;

import java.util.UUID;

@Data
public class PermissionResponse {
    private UUID id;
    private String name;
    private String description;
}
