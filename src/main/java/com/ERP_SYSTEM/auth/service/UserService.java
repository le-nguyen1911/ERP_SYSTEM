package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.AssignRoleRequest;
import com.ERP_SYSTEM.auth.dto.request.ChangePasswordRequest;
import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    /** Returns ALL users – for SUPER_ADMIN. */
    Page<UserInfoResponse> getAllUsers(Pageable pageable);

    /**
     * Returns users filtered based on the caller's privilege:
     * - SUPER_ADMIN sees all users.
     * - ADMIN and others do NOT see SUPER_ADMIN users.
     */
    Page<UserInfoResponse> getAllUsersForCaller(Pageable pageable);

    UserInfoResponse getUserById(UUID id);

    UserInfoResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);

    void lockUser(UUID id);

    void unlockUser(UUID id);

    UserInfoResponse assignRoles(UUID id, AssignRoleRequest request);

    UserInfoResponse removeRoles(UUID id, AssignRoleRequest request);

    void changePassword(String username, ChangePasswordRequest request);
}

