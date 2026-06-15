package com.ERP_SYSTEM.auth.service;

import com.ERP_SYSTEM.auth.dto.request.LoginRequest;
import com.ERP_SYSTEM.auth.dto.request.RegisterRequest;
import com.ERP_SYSTEM.auth.dto.response.ActiveSessionResponse;
import com.ERP_SYSTEM.auth.dto.response.AuthResponse;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;

import java.util.List;

public interface AuthService {

    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request, String deviceInfo);
    // Thêm deviceInfo → biết đăng nhập từ thiết bị nào

    AuthResponse refreshToken(String refreshToken);
    UserInfoResponse getMe(String username);

    void logout(String refreshToken);
    // Logout 1 thiết bị (thu hồi 1 token)

    void logoutAllDevices(String username);
    // Logout tất cả thiết bị (thu hồi hết token)

    List<ActiveSessionResponse> getActiveSessions(
            String username, String currentToken);
    // Xem danh sách thiết bị đang đăng nhập

    void revokeSession(Long tokenId, String username);
    // Admin hoặc chính user thu hồi 1 session cụ thể
}
