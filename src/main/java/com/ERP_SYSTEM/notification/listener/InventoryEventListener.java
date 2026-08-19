package com.ERP_SYSTEM.notification.listener;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.inventory.event.LowStockDetectedEvent;
import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import com.ERP_SYSTEM.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class InventoryEventListener {

    private static final String STOCK_MANAGEMENT_PERMISSION = "STOCK_UPDATE";
    
    private static final long DEDUPE_WINDOW_HOURS = 24;

    private final NotificationService notificationService;
    private final UserRepository userRepository;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLowStockDetected(LowStockDetectedEvent event) {
        log.info("Nhận sự kiện cảnh báo tồn kho thấp: product={}, warehouse={}, quantity={}/{}",
                event.productName(), event.warehouseId(),
                event.currentQuantity(), event.minQuantity());

        List<User> recipients = userRepository.findByPermissionName(STOCK_MANAGEMENT_PERMISSION);

        if (recipients.isEmpty()) {
            log.warn("Không tìm thấy người dùng nào có quyền '{}' để nhận cảnh báo tồn kho thấp",
                    STOCK_MANAGEMENT_PERMISSION);
            return;
        }

        String title = "Cảnh báo tồn kho thấp";
        String message = String.format(
                "Sản phẩm '%s' tại kho đang ở mức tồn kho thấp: còn %s (ngưỡng tối thiểu: %s).",
                event.productName(), event.currentQuantity(), event.minQuantity());

        int createdCount = 0;
        int skippedCount = 0;

        for (User recipient : recipients) {
            var result = notificationService.createIfNotDuplicate(
                    new CreateNotificationRequest(
                            recipient.getId(),
                            title,
                            message,
                            NotificationType.WARNING,
                            SourceModule.INVENTORY,
                            "Product",
                            event.productId()
                    ),
                    DEDUPE_WINDOW_HOURS
            );

            if (result.isPresent()) {
                createdCount++;
            } else {
                skippedCount++;
            }
        }

        log.info("Xử lý cảnh báo tồn kho thấp cho sản phẩm '{}': đã tạo {} thông báo mới, " +
                        "bỏ qua {} thông báo trùng lặp",
                event.productName(), createdCount, skippedCount);
    }
}
