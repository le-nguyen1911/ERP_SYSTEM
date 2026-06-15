package com.ERP_SYSTEM.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.Set;

@Data
public class CreateRoleRequest {
    @NotBlank(message = "Tên role không được để trống")
    private String name;
    private String description;
    private Set<String> permissions;
}
