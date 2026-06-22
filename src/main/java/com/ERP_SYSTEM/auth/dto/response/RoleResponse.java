package com.ERP_SYSTEM.auth.dto.response;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class RoleResponse {
    private UUID id;
    private String name;
    private String description;
    private Set<String> permissions;
}
