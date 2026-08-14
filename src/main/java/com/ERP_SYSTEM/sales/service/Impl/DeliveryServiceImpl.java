package com.ERP_SYSTEM.sales.service.Impl;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.inventory.dto.request.StockTransactionRequest;
import com.ERP_SYSTEM.inventory.dto.response.ProductStockResponse;
import com.ERP_SYSTEM.inventory.entity.StockTransaction;
import com.ERP_SYSTEM.inventory.service.StockService;
import com.ERP_SYSTEM.purchase.repository.SequenceRepository;
import com.ERP_SYSTEM.sales.dto.request.CreateDeliveryRequest;
import com.ERP_SYSTEM.sales.dto.request.DeliveryItemRequest;
import com.ERP_SYSTEM.sales.dto.response.DeliveryDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.DeliverySummaryResponse;
import com.ERP_SYSTEM.sales.entity.Delivery;
import com.ERP_SYSTEM.sales.entity.DeliveryItem;
import com.ERP_SYSTEM.sales.entity.Enum.DeliveryStatus;
import com.ERP_SYSTEM.sales.entity.Enum.InventoryExportStatus;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderItemStatus;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import com.ERP_SYSTEM.sales.entity.SalesOrder;
import com.ERP_SYSTEM.sales.entity.SalesOrderItem;
import com.ERP_SYSTEM.sales.event.DeliveryExportFailedEvent;
import com.ERP_SYSTEM.sales.mapper.DeliveryMapper;
import com.ERP_SYSTEM.sales.repository.DeliveryItemRepository;
import com.ERP_SYSTEM.sales.repository.DeliveryRepository;
import com.ERP_SYSTEM.sales.repository.SalesOrderItemRepository;
import com.ERP_SYSTEM.sales.repository.SalesOrderRepository;
import com.ERP_SYSTEM.sales.service.DeliveryService;
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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class DeliveryServiceImpl implements DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final DeliveryItemRepository deliveryItemRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final SalesOrderItemRepository salesOrderItemRepository;
    private final SequenceRepository sequenceRepository;
    private final DeliveryMapper deliveryMapper;
    private final StockService stockService;
    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Override
    @Transactional
    public DeliveryDetailResponse create(CreateDeliveryRequest request) {
        log.info("Bắt đầu tạo phiếu giao hàng cho SO id={}", request.salesOrderId());

        SalesOrder salesOrder = salesOrderRepository
                .findByIdAndIsDeletedFalse(request.salesOrderId())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn bán hàng với id: " + request.salesOrderId()));

        if (salesOrder.getStatus() != SalesOrderStatus.CONFIRMED
                && salesOrder.getStatus() != SalesOrderStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Chỉ có thể giao hàng cho đơn bán hàng đã xác nhận, trạng thái hiện tại: "
                            + salesOrder.getStatus());
        }

        Delivery delivery = Delivery.builder()
                .deliveryNumber(generateDeliveryNumber())
                .salesOrder(salesOrder)
                .customer(salesOrder.getCustomer())
                .warehouseId(salesOrder.getWarehouseId())
                .deliveryDate(LocalDateTime.now())
                .deliveredById(getCurrentUser().getId())
                .status(DeliveryStatus.DRAFT)
                .build();

        for (DeliveryItemRequest itemRequest : request.items()) {
            SalesOrderItem soItem = salesOrderItemRepository
                    .findByIdAndIsDeletedFalse(itemRequest.salesOrderItemId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Không tìm thấy dòng đơn hàng với id: "
                                    + itemRequest.salesOrderItemId()));

            if (!soItem.getSalesOrder().getId().equals(salesOrder.getId())) {
                throw new IllegalArgumentException(
                        "Dòng đơn hàng " + itemRequest.salesOrderItemId()
                                + " không thuộc đơn bán hàng " + salesOrder.getId());
            }

            BigDecimal quantityToDeliver = itemRequest.quantityDelivered();


            BigDecimal alreadyClaimed = deliveryItemRepository
                    .sumActiveClaimedQuantity(soItem.getId());

            BigDecimal remainingToSell = soItem.getQuantity().subtract(alreadyClaimed);

            if (quantityToDeliver.compareTo(remainingToSell) > 0) {
                throw new IllegalStateException(
                        "Số lượng giao vượt quá số lượng còn lại của đơn hàng cho sản phẩm "
                                + soItem.getProductName() + ". Còn lại có thể giao: " + remainingToSell
                                + " (đã có " + alreadyClaimed + " đang được xử lý ở các phiếu khác)");
            }


            List<ProductStockResponse> stocksOfProduct =
                    stockService.getStockByProduct(soItem.getProductId());

            ProductStockResponse stockAtThisWarehouse = stocksOfProduct.stream()
                    .filter(s -> s.warehouse().id().equals(salesOrder.getWarehouseId()))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException(
                            "Sản phẩm " + soItem.getProductName()
                                    + " chưa có tồn kho tại kho xuất hàng của đơn này"));

            if (BigDecimal.valueOf(stockAtThisWarehouse.quantity())
                    .compareTo(quantityToDeliver) < 0) {

                throw new IllegalStateException(
                        "Không đủ tồn kho để giao hàng cho sản phẩm " + soItem.getProductName()
                                + ". Tồn kho hiện có: " + stockAtThisWarehouse.quantity()
                                + ", cần giao: " + quantityToDeliver);
            }

            DeliveryItem deliveryItem = DeliveryItem.builder()
                    .salesOrderItem(soItem)
                    .productId(soItem.getProductId())
                    .quantityDelivered(quantityToDeliver)
                    .batchNumber(itemRequest.batchNumber())
                    .notes(itemRequest.notes())
                    .build();

            delivery.addItem(deliveryItem);
        }

        Delivery saved = deliveryRepository.save(delivery);
        log.info("Tạo phiếu giao hàng thành công, id={}, deliveryNumber={}",
                saved.getId(), saved.getDeliveryNumber());


        exportToInventory(saved);

        return deliveryMapper.toDetailResponse(saved);
    }

    private String generateDeliveryNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequenceValue = sequenceRepository.nextDeliveryNumberSequence();
        return "DEL-" + datePart + "-" + String.format("%06d", sequenceValue);
    }


    private void exportToInventory(Delivery delivery) {
        try {
            for (DeliveryItem item : delivery.getItems()) {
                String note = buildStockTransactionNote(item);

                StockTransactionRequest stockRequest = new StockTransactionRequest(
                        item.getProductId(),
                        delivery.getWarehouseId(),
                        StockTransaction.TransactionType.EXPORT,
                        item.getQuantityDelivered().intValue(),
                        item.getSalesOrderItem().getUnitPrice(),
                        note
                );

                stockService.processTransaction(stockRequest);
            }

            delivery.setInventoryExportStatus(InventoryExportStatus.SUCCESS);
            delivery.setInventoryErrorMessage(null);
            delivery.setStatus(DeliveryStatus.EXPORTED);

            updateSalesOrderItemsDeliveredQuantity(delivery);
            checkAndUpdateSalesOrderStatus(delivery.getSalesOrder());

            log.info("Xuất kho thành công cho phiếu giao hàng id={}", delivery.getId());

        } catch (Exception ex) {
            log.error("Xuất kho THẤT BẠI cho phiếu giao hàng id={}, lỗi: {}",
                    delivery.getId(), ex.getMessage(), ex);

            delivery.setInventoryExportStatus(InventoryExportStatus.FAILED);
            delivery.setInventoryErrorMessage(
                    ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName());
            delivery.setLastInventoryRetryAt(LocalDateTime.now());

            eventPublisher.publishEvent(new DeliveryExportFailedEvent(
                    delivery.getId(),
                    delivery.getDeliveryNumber(),
                    delivery.getDeliveredById(),
                    delivery.getInventoryErrorMessage()
            ));

        }
    }

    private String buildStockTransactionNote(DeliveryItem item) {
        StringBuilder note = new StringBuilder("Delivery:" + item.getDelivery().getDeliveryNumber());
        if (item.getBatchNumber() != null) {
            note.append(" | Batch:").append(item.getBatchNumber());
        }
        return note.toString();
    }

    private void updateSalesOrderItemsDeliveredQuantity(Delivery delivery) {
        for (DeliveryItem deliveryItem : delivery.getItems()) {
            SalesOrderItem soItem = deliveryItem.getSalesOrderItem();

            BigDecimal newDeliveredQuantity = soItem.getDeliveredQuantity()
                    .add(deliveryItem.getQuantityDelivered());

            soItem.setDeliveredQuantity(newDeliveredQuantity);

            if (newDeliveredQuantity.compareTo(soItem.getQuantity()) >= 0) {
                soItem.setStatus(SalesOrderItemStatus.FULLY_DELIVERED);
            } else if (newDeliveredQuantity.compareTo(BigDecimal.ZERO) > 0) {
                soItem.setStatus(SalesOrderItemStatus.PARTIALLY_DELIVERED);
            }
        }
    }

    private void checkAndUpdateSalesOrderStatus(SalesOrder salesOrder) {
        List<SalesOrderItem> allItems = salesOrderItemRepository
                .findBySalesOrderIdAndIsDeletedFalse(salesOrder.getId());

        boolean allFullyDelivered = allItems.stream()
                .allMatch(item -> item.getStatus() == SalesOrderItemStatus.FULLY_DELIVERED
                        || item.getStatus() == SalesOrderItemStatus.CANCELLED);

        if (allFullyDelivered && salesOrder.getStatus() == SalesOrderStatus.CONFIRMED) {
            salesOrder.setStatus(SalesOrderStatus.DELIVERED);
            log.info("Đơn bán hàng id={} đã giao đủ hàng, chuyển trạng thái DELIVERED",
                    salesOrder.getId());
        }
    }


    @Override
    public DeliveryDetailResponse getById(UUID id) {
        Delivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu giao hàng với id: " + id));
        return deliveryMapper.toDetailResponse(delivery);
    }

    @Override
    public Page<DeliverySummaryResponse> getAll(Pageable pageable) {
        return deliveryRepository.findByIsDeletedFalse(pageable)
                .map(deliveryMapper::toSummaryResponse);
    }

    @Override
    public Page<DeliverySummaryResponse> getBySalesOrder(UUID salesOrderId, Pageable pageable) {
        return deliveryRepository.findBySalesOrderIdAndIsDeletedFalse(salesOrderId, pageable)
                .map(deliveryMapper::toSummaryResponse);
    }


    @Override
    @Transactional
    public DeliveryDetailResponse retryInventoryExport(UUID id) {
        log.info("Thử lại xuất kho thủ công cho phiếu id={}", id);

        Delivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu giao hàng với id: " + id));

        if (delivery.getInventoryExportStatus() != InventoryExportStatus.FAILED) {
            throw new IllegalStateException(
                    "Chỉ có thể thử lại xuất kho khi trạng thái hiện tại là FAILED, hiện tại: "
                            + delivery.getInventoryExportStatus());
        }

        exportToInventory(delivery);

        return deliveryMapper.toDetailResponse(delivery);
    }

    @Override
    @Scheduled(fixedDelay = 900_000)
    @Transactional
    public void retryAllFailedInventoryExports() {
        List<Delivery> failedDeliveries =
                deliveryRepository.findByInventoryExportStatus(InventoryExportStatus.FAILED);

        if (failedDeliveries.isEmpty()) {
            return;
        }

        log.info("Scheduled Job: tìm thấy {} phiếu giao hàng cần thử lại xuất kho",
                failedDeliveries.size());

        for (Delivery delivery : failedDeliveries) {
            try {
                exportToInventory(delivery);
            } catch (Exception ex) {
                log.error("Lỗi khi retry phiếu giao hàng id={} trong Scheduled Job: {}",
                        delivery.getId(), ex.getMessage(), ex);
            }
        }
    }

    @Override
    @Transactional
    public DeliveryDetailResponse markAsDelivered(UUID id) {
        log.info("Xác nhận đã giao hàng thành công, phiếu id={}", id);

        Delivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu giao hàng với id: " + id));

        if (delivery.getStatus() != DeliveryStatus.EXPORTED) {
            throw new IllegalStateException(
                    "Chỉ có thể xác nhận đã giao hàng khi phiếu đang ở trạng thái EXPORTED, hiện tại: "
                            + delivery.getStatus());
        }
        delivery.setStatus(DeliveryStatus.DELIVERED);

        log.info("Xác nhận giao hàng thành công, id={}", id);
        return deliveryMapper.toDetailResponse(delivery);
    }


    @Override
    @Transactional
    public void cancel(UUID id, String reason) {
        log.info("Hủy phiếu giao hàng id={}, lý do={}", id, reason);

        Delivery delivery = deliveryRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy phiếu giao hàng với id: " + id));

        if (delivery.getStatus() == DeliveryStatus.EXPORTED
                || delivery.getStatus() == DeliveryStatus.DELIVERED) {
            throw new IllegalStateException(
                    "Không thể hủy phiếu giao hàng đã xuất kho thành công");
        }

        delivery.setStatus(DeliveryStatus.CANCELLED);
        delivery.setRejectionReason(reason);

        log.info("Hủy phiếu giao hàng thành công, id={}", id);
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
