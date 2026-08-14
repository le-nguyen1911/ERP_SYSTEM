package com.ERP_SYSTEM.notification.listener;

import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;
import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import com.ERP_SYSTEM.notification.service.NotificationService;
import com.ERP_SYSTEM.sales.event.DeliveryExportFailedEvent;
import com.ERP_SYSTEM.sales.event.SalesOrderApprovedEvent;
import com.ERP_SYSTEM.sales.event.SalesOrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class SaleEventListener {
    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSalesOrderApproved(SalesOrderApprovedEvent event) {
        log.info("Nhận sự kiện SO duyệt, id={}, tạo thông báo cho user={}",
                event.salesOrderId(), event.createdById());

        if (event.createdById() == null) {
            log.warn("SalesOrder id={} không có createdById, bỏ qua tạo thông báo",
                    event.salesOrderId());
            return;
        }

        notificationService.create(new CreateNotificationRequest(
                event.createdById(),
                "Đơn bán hàng đã được duyệt",
                "Đơn bán hàng " + event.soNumber() + " của bạn đã được duyệt.",
                NotificationType.SUCCESS,
                SourceModule.SALES,
                "SalesOrder",
                event.salesOrderId()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSalesOrderRejected(SalesOrderRejectedEvent event) {
        log.info("Nhận sự kiện SO từ chối, id={}, tạo thông báo cho user={}",
                event.salesOrderId(), event.createdById());

        if (event.createdById() == null) {
            return;
        }

        notificationService.create(new CreateNotificationRequest(
                event.createdById(),
                "Đơn bán hàng bị từ chối",
                "Đơn bán hàng " + event.soNumber() + " đã bị từ chối. Lý do: "
                        + event.rejectionReason(),
                NotificationType.WARNING,
                SourceModule.SALES,
                "SalesOrder",
                event.salesOrderId()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onDeliveryExportFailed(DeliveryExportFailedEvent event) {
        log.info("Nhận sự kiện xuất kho thất bại, phiếu id={}", event.deliveryId());

        notificationService.create(new CreateNotificationRequest(
                event.deliveredById(),
                "Xuất kho thất bại",
                "Phiếu giao hàng " + event.deliveryNumber() + " gặp lỗi khi xuất kho: "
                        + event.errorMessage() + ". Hệ thống sẽ tự động thử lại.",
                NotificationType.ERROR,
                SourceModule.SALES,
                "Delivery",
                event.deliveryId()
        ));
    }
}