package com.ERP_SYSTEM.auth.dto.response;

import lombok.Data;

import java.util.Set;

@Data
public class UserInfoResponse {
    private Long id;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private Boolean enabled;
    private Set<String> roles;
}
