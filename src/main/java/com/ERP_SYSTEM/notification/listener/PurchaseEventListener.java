package com.ERP_SYSTEM.notification.listener;


import com.ERP_SYSTEM.notification.dto.request.CreateNotificationRequest;

import com.ERP_SYSTEM.notification.entity.Enum.NotificationType;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import com.ERP_SYSTEM.notification.service.NotificationService;
import com.ERP_SYSTEM.purchase.event.GoodsReceiptImportFailedEvent;
import com.ERP_SYSTEM.purchase.event.PurchaseOrderApprovedEvent;
import com.ERP_SYSTEM.purchase.event.PurchaseOrderRejectedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class PurchaseEventListener {

    private final NotificationService notificationService;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPurchaseOrderApproved(PurchaseOrderApprovedEvent event) {
        log.info("Nhận sự kiện PO duyệt, id={}, tạo thông báo cho user={}",
                event.purchaseOrderId(), event.createdById());

        // Nếu PO này được tạo trước khi có migration V5 (createdById =
        // null với dữ liệu cũ), bỏ qua không tạo thông báo - tránh lỗi
        // NPE hoặc tạo thông báo với recipientId=null vô nghĩa.
        if (event.createdById() == null) {
            log.warn("PurchaseOrder id={} không có createdById (dữ liệu cũ trước migration V5), " +
                    "bỏ qua tạo thông báo", event.purchaseOrderId());
            return;
        }

        notificationService.create(new CreateNotificationRequest(
                event.createdById(),
                "Đơn đặt hàng đã được duyệt",
                "Đơn đặt hàng " + event.poNumber() + " của bạn đã được duyệt.",
                NotificationType.SUCCESS,
                SourceModule.PURCHASE,
                "PurchaseOrder",
                event.purchaseOrderId()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPurchaseOrderRejected(PurchaseOrderRejectedEvent event) {
        log.info("Nhận sự kiện PO từ chối, id={}, tạo thông báo cho user={}",
                event.purchaseOrderId(), event.createdById());

        if (event.createdById() == null) {
            return;
        }

        notificationService.create(new CreateNotificationRequest(
                event.createdById(),
                "Đơn đặt hàng bị từ chối",
                "Đơn đặt hàng " + event.poNumber() + " đã bị từ chối. Lý do: "
                        + event.rejectionReason(),
                NotificationType.WARNING,
                SourceModule.PURCHASE,
                "PurchaseOrder",
                event.purchaseOrderId()
        ));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onGoodsReceiptImportFailed(GoodsReceiptImportFailedEvent event) {
        log.info("Nhận sự kiện import kho thất bại, phiếu id={}", event.goodsReceiptId());

        notificationService.create(new CreateNotificationRequest(
                event.receivedById(),
                "Nhập kho thất bại",
                "Phiếu nhận hàng " + event.grNumber() + " gặp lỗi khi nhập kho: "
                        + event.errorMessage() + ". Hệ thống sẽ tự động thử lại.",
                NotificationType.ERROR,
                SourceModule.PURCHASE,
                "GoodsReceipt",
                event.goodsReceiptId()
        ));
    }
}