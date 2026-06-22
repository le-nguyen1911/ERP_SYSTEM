package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.AssignRoleRequest;
import com.ERP_SYSTEM.auth.dto.request.ChangePasswordRequest;
import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface UserService {
    Page<UserInfoResponse> getAllUsers(Pageable pageable);
    // Trả về danh sách có phân trang
    // Không trả hết 1000 user 1 lúc → nặng server

    UserInfoResponse getUserById(UUID id);

    UserInfoResponse updateUser(UUID id, UpdateUserRequest request);

    void deleteUser(UUID id);

    void lockUser(UUID id);
    // enabled = false → không login được

    void unlockUser(UUID id);
    // enabled = true → login được lại

    UserInfoResponse assignRoles(UUID id, AssignRoleRequest request);
    // Gán role cho user

    UserInfoResponse removeRoles(UUID id, AssignRoleRequest request);
    // Xóa role khỏi user

    void changePassword(String username, ChangePasswordRequest request);
    // User tự đổi mật khẩu của mình
}
