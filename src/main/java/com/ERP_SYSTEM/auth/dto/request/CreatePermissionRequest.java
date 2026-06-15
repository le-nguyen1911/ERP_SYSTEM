package com.ERP_SYSTEM.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreatePermissionRequest {
    @NotBlank(message = "Tên permission không được để trống")
    private String name;
    private String description;

}
