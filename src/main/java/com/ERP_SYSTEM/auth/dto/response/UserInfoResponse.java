package com.ERP_SYSTEM.auth.dto.response;

import lombok.Data;

import java.util.Set;
import java.util.UUID;

@Data
public class UserInfoResponse {
    private UUID id;
    private String username;
    private String email;
    private String fullName;
    private String avatar;
    private Boolean enabled;
    private Set<String> roles;
}
