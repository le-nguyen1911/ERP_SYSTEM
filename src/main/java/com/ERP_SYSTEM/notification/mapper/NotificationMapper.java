package com.ERP_SYSTEM.notification.mapper;

import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.dto.response.NotificationResponse;
import com.ERP_SYSTEM.notification.entity.Notification;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificationMapper {

    NotificationResponse toResponse(Notification notification);
    
    Notification toEntity(CreateNotificationRequest request);
}