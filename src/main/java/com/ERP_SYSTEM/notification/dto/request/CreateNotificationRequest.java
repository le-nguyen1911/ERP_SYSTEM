package com.ERP_SYSTEM.notification.dto.request;

import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.UUID;

public record CreateNotificationRequest(
        @NotNull(message = "Người nhận không được để trống")
        UUID recipientId,

        @NotBlank(message = "Tiêu đề không được để trống")
        @Size(max = 255)
        String title,

        @NotBlank(message = "Nội dung không được để trống")
        String message,

        NotificationType type,

        @NotNull(message = "Module nguồn không được để trống")
        SourceModule module,

        String referenceType,

        UUID referenceId
) {

}
