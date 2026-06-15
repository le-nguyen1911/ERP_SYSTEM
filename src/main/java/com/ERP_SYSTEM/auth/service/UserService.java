package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.AssignRoleRequest;
import com.ERP_SYSTEM.auth.dto.request.ChangePasswordRequest;
import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {
    Page<UserInfoResponse> getAllUsers(Pageable pageable);
    // Trả về danh sách có phân trang
    // Không trả hết 1000 user 1 lúc → nặng server

    UserInfoResponse getUserById(Long id);

    UserInfoResponse updateUser(Long id, UpdateUserRequest request);

    void deleteUser(Long id);

    void lockUser(Long id);
    // enabled = false → không login được

    void unlockUser(Long id);
    // enabled = true → login được lại

    UserInfoResponse assignRoles(Long id, AssignRoleRequest request);
    // Gán role cho user

    UserInfoResponse removeRoles(Long id, AssignRoleRequest request);
    // Xóa role khỏi user

    void changePassword(String username, ChangePasswordRequest request);
    // User tự đổi mật khẩu của mình
}
