package com.ERP_SYSTEM.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "Username không được bỏ trống")
    @Size(min = 6,max = 30, message = "Username từ 6-30 ký tự")
    private String username;
    @NotBlank(message = "Password không được để trống")
    @Size(min = 6, message = "Password tối thiểu 6 ký tự")
    private String password;
}
