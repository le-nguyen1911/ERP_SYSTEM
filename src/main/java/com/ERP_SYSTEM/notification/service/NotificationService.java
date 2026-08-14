package com.ERP_SYSTEM.notification.service;

import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.dto.response.NotificationResponse;
import com.ERP_SYSTEM.notification.dto.response.UnreadCountResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface NotificationService {
    NotificationResponse create(CreateNotificationRequest request);

    Page<NotificationResponse> getMyNotifications(UUID recipientId, Pageable pageable);

    Page<NotificationResponse> getMyUnreadNotifications(UUID recipientId, Pageable pageable);

    UnreadCountResponse getUnreadCount(UUID recipientId);

    NotificationResponse markAsRead(UUID id, UUID currentUserId);

    void markAllAsRead(UUID recipientId);

    void delete(UUID id, UUID currentUserId);
}
