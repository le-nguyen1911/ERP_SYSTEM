package com.ERP_SYSTEM.purchase.service.Implement;

import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.inventory.dto.request.StockTransactionRequest;
import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import com.ERP_SYSTEM.inventory.service.StockService;
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
import com.ERP_SYSTEM.purchase.dto.request.CreateGoodsReceiptRequest;
import com.ERP_SYSTEM.purchase.dto.request.GoodsReceiptItemRequest;
import com.ERP_SYSTEM.purchase.dto.request.QualityCheckRequest;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.GoodsReceiptSummaryResponse;
import com.ERP_SYSTEM.purchase.entity.GoodsReceipt;
import com.ERP_SYSTEM.purchase.entity.GoodsReceiptItem;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrder;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrderItem;
import com.ERP_SYSTEM.purchase.enums.*;
import com.ERP_SYSTEM.purchase.event.GoodsReceiptImportFailedEvent;
import com.ERP_SYSTEM.purchase.mapper.GoodsReceiptMapper;
import com.ERP_SYSTEM.purchase.repository.*;
import com.ERP_SYSTEM.purchase.service.GoodsReceiptService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GoodsReceiptServiceImpl implements GoodsReceiptService {

    private final GoodsReceiptRepository goodsReceiptRepository;
    private final GoodsReceiptItemRepository goodsReceiptItemRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final PurchaseOrderItemRepository purchaseOrderItemRepository;
    private final SequenceRepository sequenceRepository;
    private final GoodsReceiptMapper goodsReceiptMapper;
    private final UserRepository userRepository;
    private final StockService stockService;
    private final ApplicationEventPublisher eventPublisher;

    @Auditable(
            entityClass = GoodsReceipt.class,
            entityType = "GoodsReceipt",
            action = AuditAction.CREATE,
            module = SourceModule.PURCHASE,
            idExpression = "#result.id"
    )
    @Override
    @Transactional
    public GoodsReceiptDetailResponse create(CreateGoodsReceiptRequest request) {
        log.info("Bắt đầu tạo phiếu nhận hàng cho PO id={}", request.purchaseOrderId());

        PurchaseOrder purchaseOrder = purchaseOrderRepository
                .findByIdAndIsDeletedFalse(request.purchaseOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn đặt hàng với id: " + request.purchaseOrderId()));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.SENT_TO_SUPPLIER
                && purchaseOrder.getStatus() != PurchaseOrderStatus.GOODS_RECEIVED) {
            throw new IllegalStateException(
                    "Chỉ có thể nhận hàng cho đơn đặt hàng đã gửi nhà cung cấp, trạng thái hiện tại: "
                            + purchaseOrder.getStatus());
        }

        GoodsReceipt goodsReceipt = GoodsReceipt.builder()
                .grNumber(generateGrNumber())
                .purchaseOrder(purchaseOrder)
                .supplier(purchaseOrder.getSupplier())
                .warehouseId(purchaseOrder.getWarehouseId())
                .grDate(LocalDateTime.now())
                .receivedById(getCurrentUser().getId())
                .status(GoodsReceiptStatus.DRAFT)
                .build();

        for (GoodsReceiptItemRequest itemRequest : request.items()) {
            PurchaseOrderItem poItem = purchaseOrderItemRepository
                    .findByIdAndIsDeletedFalse(itemRequest.purchaseOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy dòng đơn hàng với id: "
                                    + itemRequest.purchaseOrderItemId()));

            if (!poItem.getPurchaseOrder().getId().equals(purchaseOrder.getId())) {
                throw new IllegalArgumentException(
                        "Dòng đơn hàng " + itemRequest.purchaseOrderItemId()
                                + " không thuộc đơn đặt hàng " + purchaseOrder.getId());
            }

            BigDecimal quantityAccepted = itemRequest.quantityAccepted();
            BigDecimal quantityRejected = itemRequest.quantityRejected() != null
                    ? itemRequest.quantityRejected() : BigDecimal.ZERO;

            BigDecimal alreadyClaimed = goodsReceiptItemRepository
                    .sumActiveClaimedQuantity(poItem.getId());

            BigDecimal remainingQuantity = poItem.getQuantity().subtract(alreadyClaimed);

            if (quantityAccepted.add(quantityRejected).compareTo(remainingQuantity) > 0) {
                throw new IllegalStateException(
                        "Số lượng nhận (chấp nhận + từ chối) vượt quá số lượng còn lại có thể nhận của sản phẩm "
                                + poItem.getProductName() + ". Còn lại: " + remainingQuantity
                                + " (đã có " + alreadyClaimed + " đang được xử lý ở các phiếu khác)");
            }

            GoodsReceiptItem grItem = GoodsReceiptItem.builder()
                    .purchaseOrderItem(poItem)
                    .productId(poItem.getProductId())
                    .quantityAccepted(quantityAccepted)
                    .quantityRejected(quantityRejected)
                    .batchNumber(itemRequest.batchNumber())
                    .expiryDate(itemRequest.expiryDate())
                    .notes(itemRequest.notes())
                    .build();

            goodsReceipt.addItem(grItem);
        }

        GoodsReceipt saved = goodsReceiptRepository.save(goodsReceipt);
        log.info("Tạo phiếu nhận hàng thành công, id={}, grNumber={}",
                saved.getId(), saved.getGrNumber());

        return goodsReceiptMapper.toDetailResponse(saved);
    }

    private String generateGrNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequenceValue = sequenceRepository.nextGrNumberSequence();
        return "GR-" + datePart + "-" + String.format("%06d", sequenceValue);
    }

    @Override
    public GoodsReceiptDetailResponse getById(UUID id) {
        GoodsReceipt goodsReceipt = goodsReceiptRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu nhận hàng với id: " + id));
        return goodsReceiptMapper.toDetailResponse(goodsReceipt);
    }

    @Override
    public Page<GoodsReceiptSummaryResponse> getAll(Pageable pageable) {
        return goodsReceiptRepository.findByIsDeletedFalse(pageable)
                .map(goodsReceiptMapper::toSummaryResponse);
    }

    @Override
    public Page<GoodsReceiptSummaryResponse> getByPurchaseOrder(UUID purchaseOrderId, Pageable pageable) {
        return goodsReceiptRepository.findByPurchaseOrderIdAndIsDeletedFalse(purchaseOrderId, pageable)
                .map(goodsReceiptMapper::toSummaryResponse);
    }


    @Auditable(
            entityClass = GoodsReceipt.class,
            entityType = "GoodsReceipt",
            action = AuditAction.STATUS_CHANGE,
            module = SourceModule.PURCHASE
    )
    @Override
    @Transactional
    public GoodsReceiptDetailResponse markAsReceived(UUID id) {
        log.info("Xác nhận đã nhận hàng, phiếu id={}", id);

        GoodsReceipt goodsReceipt = getForUpdateOrThrow(id);

        if (goodsReceipt.getStatus() != GoodsReceiptStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ có thể xác nhận nhận hàng khi phiếu đang ở trạng thái DRAFT");
        }

        goodsReceipt.setStatus(GoodsReceiptStatus.RECEIVED);

        log.info("Xác nhận nhận hàng thành công, id={}", id);
        return goodsReceiptMapper.toDetailResponse(goodsReceipt);
    }


    @Auditable(
            entityClass = GoodsReceipt.class,
            entityType = "GoodsReceipt",
            action = AuditAction.STATUS_CHANGE,
            module = SourceModule.PURCHASE)
    @Override
    @Transactional
    public GoodsReceiptDetailResponse performQualityCheck(UUID id, QualityCheckRequest request) {
        log.info("Thực hiện QC cho phiếu nhận hàng id={}, kết quả={}", id, request.result());

        if (request.result() == QualityCheckStatus.PENDING) {
            throw new IllegalArgumentException(
                    "Kết quả kiểm tra chất lượng phải là PASSED hoặc FAILED, không thể là PENDING");
        }

        GoodsReceipt goodsReceipt = getForUpdateOrThrow(id);

        if (goodsReceipt.getStatus() != GoodsReceiptStatus.RECEIVED) {
            throw new IllegalStateException(
                    "Chỉ có thể thực hiện QC khi phiếu đang ở trạng thái RECEIVED, hiện tại: "
                            + goodsReceipt.getStatus());
        }

        goodsReceipt.setQualityCheckStatus(request.result());
        goodsReceipt.setQualityCheckNotes(request.notes());
        goodsReceipt.setQualityCheckedById(getCurrentUser().getId());
        goodsReceipt.setQualityCheckDate(LocalDateTime.now());

        if (request.result() == QualityCheckStatus.FAILED) {
            goodsReceipt.setStatus(GoodsReceiptStatus.QC_FAILED);
            log.info("QC thất bại cho phiếu id={}, không nhập kho", id);
            return goodsReceiptMapper.toDetailResponse(goodsReceipt);
        }

        goodsReceipt.setStatus(GoodsReceiptStatus.QC_PASSED);
        importToInventory(goodsReceipt);

        log.info("QC hoàn tất cho phiếu id={}, trạng thái cuối={}", id, goodsReceipt.getStatus());
        return goodsReceiptMapper.toDetailResponse(goodsReceipt);
    }


    private void importToInventory(GoodsReceipt goodsReceipt) {
        try {
            for (GoodsReceiptItem item : goodsReceipt.getItems()) {
                String note = buildStockTransactionNote(item);

                StockTransactionRequest stockRequest = new StockTransactionRequest(
                        item.getProductId(),
                        goodsReceipt.getWarehouseId(),
                        StockTransaction.TransactionType.IMPORT,
                        item.getQuantityAccepted().intValue(),
                        item.getPurchaseOrderItem().getUnitPrice(),
                        note
                );

                stockService.processTransaction(stockRequest);
            }

            goodsReceipt.setInventoryImportStatus(InventoryImportStatus.SUCCESS);
            goodsReceipt.setInventoryErrorMessage(null);
            goodsReceipt.setStatus(GoodsReceiptStatus.IMPORTED);

            updatePurchaseOrderItemsReceivedQuantity(goodsReceipt);
            checkAndUpdatePurchaseOrderStatus(goodsReceipt.getPurchaseOrder());

            log.info("Import tồn kho thành công cho phiếu nhận hàng id={}", goodsReceipt.getId());

        } catch (Exception ex) {
            log.error("Import tồn kho THẤT BẠI cho phiếu nhận hàng id={}, lỗi: {}",
                    goodsReceipt.getId(), ex.getMessage(), ex);

            goodsReceipt.setInventoryImportStatus(InventoryImportStatus.FAILED);
            goodsReceipt.setInventoryErrorMessage(
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            goodsReceipt.setLastInventoryRetryAt(LocalDateTime.now());

            eventPublisher.publishEvent(new GoodsReceiptImportFailedEvent(
                    goodsReceipt.getId(),
                    goodsReceipt.getGrNumber(),
                    goodsReceipt.getReceivedById(),
                    goodsReceipt.getInventoryErrorMessage()
            ));
        }
    }

    private String buildStockTransactionNote(GoodsReceiptItem item) {
        StringBuilder note = new StringBuilder("GoodsReceipt:" + item.getGoodsReceipt().getGrNumber());
        if (item.getBatchNumber() != null) {
            note.append(" | Batch:").append(item.getBatchNumber());
        }
        if (item.getExpiryDate() != null) {
            note.append(" | Expiry:").append(item.getExpiryDate());
        }
        return note.toString();
    }

    private void updatePurchaseOrderItemsReceivedQuantity(GoodsReceipt goodsReceipt) {
        for (GoodsReceiptItem grItem : goodsReceipt.getItems()) {
            PurchaseOrderItem poItem = grItem.getPurchaseOrderItem();

            BigDecimal newReceivedQuantity = poItem.getReceivedQuantity()
                    .add(grItem.getQuantityAccepted());
            BigDecimal newRejectedQuantity = poItem.getRejectedQuantity()
                    .add(grItem.getQuantityRejected());

            poItem.setReceivedQuantity(newReceivedQuantity);
            poItem.setRejectedQuantity(newRejectedQuantity);

            if (newReceivedQuantity.compareTo(poItem.getQuantity()) >= 0) {
                poItem.setStatus(PurchaseOrderItemStatus.FULLY_RECEIVED);
            } else if (newReceivedQuantity.compareTo(BigDecimal.ZERO) > 0) {
                poItem.setStatus(PurchaseOrderItemStatus.PARTIALLY_RECEIVED);
            }
        }
    }

    private void checkAndUpdatePurchaseOrderStatus(PurchaseOrder purchaseOrder) {
        List<PurchaseOrderItem> allItems = purchaseOrderItemRepository
                .findByPurchaseOrderIdAndIsDeletedFalse(purchaseOrder.getId());

        boolean allFullyReceived = allItems.stream()
                .allMatch(item -> item.getStatus() == PurchaseOrderItemStatus.FULLY_RECEIVED
                        || item.getStatus() == PurchaseOrderItemStatus.CANCELLED);

        if (allFullyReceived && purchaseOrder.getStatus() == PurchaseOrderStatus.SENT_TO_SUPPLIER) {
            purchaseOrder.setStatus(PurchaseOrderStatus.GOODS_RECEIVED);
            log.info("Đơn đặt hàng id={} đã nhận đủ hàng, chuyển trạng thái GOODS_RECEIVED",
                    purchaseOrder.getId());
        }
    }

    @Auditable(
            entityClass = GoodsReceipt.class,
            entityType = "GoodsReceipt",
            action = AuditAction.UPDATE,
            module = SourceModule.PURCHASE
    )
    @Override
    @Transactional
    public GoodsReceiptDetailResponse retryInventoryImport(UUID id) {
        log.info("Thử lại import tồn kho thủ công cho phiếu id={}", id);

        GoodsReceipt goodsReceipt = getForUpdateOrThrow(id);

        if (goodsReceipt.getInventoryImportStatus() != InventoryImportStatus.FAILED) {
            throw new IllegalStateException(
                    "Chỉ có thể thử lại import khi trạng thái hiện tại là FAILED, hiện tại: "
                            + goodsReceipt.getInventoryImportStatus());
        }

        importToInventory(goodsReceipt);

        return goodsReceiptMapper.toDetailResponse(goodsReceipt);
    }

    @Override
    @Scheduled(fixedDelay = 900_000)
    @Transactional
    public void retryAllFailedInventoryImports() {
        List<GoodsReceipt> failedReceipts =
                goodsReceiptRepository.findByInventoryImportStatus(InventoryImportStatus.FAILED);

        if (failedReceipts.isEmpty()) {
            return;
        }

        log.info("Scheduled Job: tìm thấy {} phiếu nhận hàng cần thử lại import tồn kho",
                failedReceipts.size());

        for (GoodsReceipt goodsReceipt : failedReceipts) {
            try {
                importToInventory(goodsReceipt);
            } catch (Exception ex) {
                log.error("Lỗi khi retry phiếu nhận hàng id={} trong Scheduled Job: {}",
                        goodsReceipt.getId(), ex.getMessage(), ex);
            }
        }
    }

    @Auditable(
            entityClass = GoodsReceipt.class,
            entityType = "GoodsReceipt",
            action = AuditAction.STATUS_CHANGE,
            module = SourceModule.PURCHASE
    )
    @Override
    @Transactional
    public void cancel(UUID id, String reason) {
        log.info("Hủy phiếu nhận hàng id={}, lý do={}", id, reason);

        GoodsReceipt goodsReceipt = getForUpdateOrThrow(id);

        if (goodsReceipt.getStatus() == GoodsReceiptStatus.IMPORTED) {
            throw new IllegalStateException(
                    "Không thể hủy phiếu nhận hàng đã nhập kho thành công");
        }

        goodsReceipt.setStatus(GoodsReceiptStatus.CANCELLED);
        goodsReceipt.setRejectionReason(reason);

        log.info("Hủy phiếu nhận hàng thành công, id={}", id);
    }


    private GoodsReceipt getForUpdateOrThrow(UUID id) {
        return goodsReceiptRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu nhận hàng với id: " + id));
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