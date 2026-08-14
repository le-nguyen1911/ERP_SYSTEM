package com.ERP_SYSTEM.notification.controller;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.common.response.ApiResponse;
import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.dto.response.NotificationResponse;
import com.ERP_SYSTEM.notification.dto.response.UnreadCountResponse;
import com.ERP_SYSTEM.notification.service.NotificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationController {
    private final NotificationService notificationService;
    private final UserRepository userRepository;


    @PostMapping
    public ResponseEntity<ApiResponse<NotificationResponse>> create(
            @Valid @RequestBody CreateNotificationRequest request) {
        NotificationResponse response = notificationService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Tạo thông báo thành công", response));
    }

    @GetMapping("/unread")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyUnreadNotifications(
            Authentication authentication, Pageable pageable) {
        UUID userId = getCurrentUser().getId();
        Page<NotificationResponse> response =
                notificationService.getMyUnreadNotifications(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount(
            Authentication authentication) {
        UUID userId = getCurrentUser().getId();
        UnreadCountResponse response = notificationService.getUnreadCount(userId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PatchMapping("/{id}/read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = getCurrentUser().getId();
        NotificationResponse response = notificationService.markAsRead(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu đã đọc thành công", response));
    }

    @PatchMapping("/read-all")
    public ResponseEntity<ApiResponse<Void>> markAllAsRead(Authentication authentication) {
        UUID userId = getCurrentUser().getId();
        notificationService.markAllAsRead(userId);
        return ResponseEntity.ok(ApiResponse.success("Đánh dấu tất cả đã đọc thành công", null));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(
            @PathVariable UUID id, Authentication authentication) {
        UUID userId = getCurrentUser().getId();
        notificationService.delete(id, userId);
        return ResponseEntity.ok(ApiResponse.success("Xoá thông báo thành công", null));
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Người dùng chưa được xác thực");
        }

        String username = authentication.getName();

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy người dùng: " + username));
    }
}
