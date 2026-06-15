package com.ERP_SYSTEM.auth.dto.request;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRoleRequest {
    @NotEmpty(message = "Danh sách role không được trống")
    private Set<String> roles;
}
