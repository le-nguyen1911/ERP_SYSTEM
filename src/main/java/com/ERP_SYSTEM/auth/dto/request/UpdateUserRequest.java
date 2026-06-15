package com.ERP_SYSTEM.auth.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UpdateUserRequest {
    @Email(message = "Email Không hợp lệ")
    private String email;
    private String fullname;
    private String avatar;
}
