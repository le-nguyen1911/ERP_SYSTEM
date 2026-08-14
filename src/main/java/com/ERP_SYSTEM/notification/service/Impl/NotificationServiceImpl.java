package com.ERP_SYSTEM.notification.service.Impl;

import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.dto.response.NotificationResponse;
import com.ERP_SYSTEM.notification.dto.response.UnreadCountResponse;
import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Notification;
import com.ERP_SYSTEM.notification.mapper.NotificationMapper;
import com.ERP_SYSTEM.notification.repository.NotificationRepository;
import com.ERP_SYSTEM.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class NotificationServiceImpl implements NotificationService {
    private final NotificationRepository notificationRepository;
    private final NotificationMapper notificationMapper;

    @Override
    @Transactional
    public NotificationResponse create(CreateNotificationRequest request) {
        Notification notification = notificationMapper.toEntity(request);
        if (notification.getType() == null) {
            notification.setType(NotificationType.INFO);
        }
        notification.setIsRead(false);
        Notification saved = notificationRepository.save(notification);
        log.info("Tạo thông báo thành công, id={}, recipientId={}, title={}",
                saved.getId(), saved.getRecipientId(), saved.getTitle());

        return notificationMapper.toResponse(saved);
    }

    @Override
    public Page<NotificationResponse> getMyNotifications(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientId(recipientId, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public Page<NotificationResponse> getMyUnreadNotifications(UUID recipientId, Pageable pageable) {
        return notificationRepository.findByRecipientIdAndIsRead(recipientId, false, pageable)
                .map(notificationMapper::toResponse);
    }

    @Override
    public UnreadCountResponse getUnreadCount(UUID recipientId) {
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(recipientId);
        return new UnreadCountResponse(count);
    }

    @Override
    @Transactional
    public NotificationResponse markAsRead(UUID id, UUID currentUserId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy thong báo với id={}", id));
        if (!notification.getRecipientId().equals(currentUserId)) {
            throw new IllegalStateException(
                    "Bạn không có quyền thao tác trên thông báo này");
        }

        if (Boolean.FALSE.equals(notification.getIsRead())) {
            notification.setIsRead(true);
            notification.setReadAt(LocalDateTime.now());
        }

        return notificationMapper.toResponse(notification);
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID recipientId) {
        int updatedCount = notificationRepository.markAllAsRead(recipientId);
        log.info("Đánh dấu đã đọc {} thông báo cho recipientId={}", updatedCount, recipientId);
    }

    @Override
    @Transactional
    public void delete(UUID id, UUID currentUserId) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy thông báo với id: " + id));

        if (!notification.getRecipientId().equals(currentUserId)) {
            throw new IllegalStateException(
                    "Bạn không có quyền thao tác trên thông báo này");
        }
        notificationRepository.delete(notification);
    }
}
