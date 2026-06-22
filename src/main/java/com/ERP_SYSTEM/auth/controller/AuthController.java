package com.ERP_SYSTEM.auth.controller;

import com.ERP_SYSTEM.auth.dto.request.LoginRequest;
import com.ERP_SYSTEM.auth.dto.request.RegisterRequest;
import com.ERP_SYSTEM.auth.dto.response.ActiveSessionResponse;
import com.ERP_SYSTEM.auth.dto.response.AuthResponse;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import com.ERP_SYSTEM.auth.service.AuthService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success("Đăng ký thành công", authService.register(request))
        );
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(@Valid @RequestBody LoginRequest request, HttpServletRequest httpServletRequest) {

        String deviceInfo = extractDeviceInfo(httpServletRequest);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng nhập thành công", authService.login(request, deviceInfo))
        );
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthResponse>> refreshToken(@RequestHeader("Refresh-Token") String refreshToken) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        authService.refreshToken(refreshToken)
                )
        );
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getMe(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        authService.getMe(user.getUsername())
                )
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(
            @RequestHeader("Refresh-Token") String refreshToken) {
        // Logout thiết bị hiện tại
        // Client gửi refresh token để server biết
        // token nào cần thu hồi
        authService.logout(refreshToken);
        return ResponseEntity.ok(
                ApiResponse.success("Đăng xuất thành công", null)
        );
    }

    @PostMapping("/logout-all")
    public ResponseEntity<ApiResponse<Object>> logoutAll(
            @AuthenticationPrincipal UserDetails userDetails) {
        // Logout TẤT CẢ thiết bị
        // Dùng username từ access token hiện tại
        authService.logoutAllDevices(userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Đăng xuất tất cả thiết bị thành công", null)
        );
    }

    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<List<ActiveSessionResponse>>> getSessions(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestHeader("Refresh-Token") String currentToken) {
        // Xem danh sách thiết bị đang đăng nhập
        // currentToken để đánh dấu thiết bị hiện tại
        return ResponseEntity.ok(
                ApiResponse.success(
                        authService.getActiveSessions(
                                userDetails.getUsername(), currentToken))
        );
    }

    @DeleteMapping("/sessions/{id}")
    public ResponseEntity<ApiResponse<Object>> revokeSession(
            @PathVariable UUID id,
            @AuthenticationPrincipal UserDetails userDetails) {
        // Thu hồi 1 session cụ thể
        // Ví dụ: đăng xuất khỏi máy tính cũ từ xa
        authService.revokeSession(id, userDetails.getUsername());
        return ResponseEntity.ok(
                ApiResponse.success("Thu hồi session thành công", null)
        );
    }


    // Helper: lấy thông tin thiết bị từ User-Agent
    private String extractDeviceInfo(HttpServletRequest request) {
        String userAgent = request.getHeader("User-Agent");
        // User-Agent: "Mozilla/5.0 (Windows NT 10.0; Win64; x64)
        //              AppleWebKit/537.36 Chrome/91.0..."

        if (userAgent == null) return "Unknown Device";

        // Phân tích User-Agent đơn giản
        if (userAgent.contains("Mobile")) {
            return "Mobile Browser";
        } else if (userAgent.contains("Chrome")) {
            return "Chrome Browser";
        } else if (userAgent.contains("Firefox")) {
            return "Firefox Browser";
        } else if (userAgent.contains("Safari")) {
            return "Safari Browser";
        }
        return "Web Browser";
    }

}
