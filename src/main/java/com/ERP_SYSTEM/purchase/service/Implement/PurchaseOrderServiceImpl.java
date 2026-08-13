package com.ERP_SYSTEM.purchase.service.Implement;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.purchase.dto.request.*;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderDetailResponse;
import com.ERP_SYSTEM.purchase.dto.response.PurchaseOrderSummaryResponse;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrder;
import com.ERP_SYSTEM.purchase.entity.PurchaseOrderItem;
import com.ERP_SYSTEM.purchase.entity.Supplier;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;
import com.ERP_SYSTEM.purchase.enums.SupplierStatus;
import com.ERP_SYSTEM.purchase.mapper.PurchaseOrderMapper;
import com.ERP_SYSTEM.purchase.repository.PurchaseOrderRepository;
import com.ERP_SYSTEM.purchase.repository.SequenceRepository;
import com.ERP_SYSTEM.purchase.repository.SupplierRepository;
import com.ERP_SYSTEM.purchase.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final UserRepository userRepository;
    private final SequenceRepository sequenceRepository;


    private static final Map<PurchaseOrderStatus, Set<PurchaseOrderStatus>> VALID_TRANSITIONS =
            new EnumMap<>(PurchaseOrderStatus.class);

    static {
        VALID_TRANSITIONS.put(PurchaseOrderStatus.DRAFT,
                EnumSet.of(PurchaseOrderStatus.PENDING_APPROVAL, PurchaseOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(PurchaseOrderStatus.PENDING_APPROVAL,
                EnumSet.of(PurchaseOrderStatus.APPROVED, PurchaseOrderStatus.REJECTED));
        VALID_TRANSITIONS.put(PurchaseOrderStatus.APPROVED,
                EnumSet.of(PurchaseOrderStatus.SENT_TO_SUPPLIER, PurchaseOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(PurchaseOrderStatus.SENT_TO_SUPPLIER,
                EnumSet.of(PurchaseOrderStatus.GOODS_RECEIVED, PurchaseOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(PurchaseOrderStatus.GOODS_RECEIVED,
                EnumSet.of(PurchaseOrderStatus.CLOSED));
    }

    private void validateTransition(PurchaseOrderStatus from, PurchaseOrderStatus to) {
        Set<PurchaseOrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalStateException(
                    "Không thể chuyển đơn hàng từ trạng thái " + from + " sang " + to);
        }
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse create(CreatePurchaseOrderRequest request) {
        log.info("Bắt đầu tạo đơn đặt hàng cho supplier={}", request.supplierId());
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cng cấp"));
        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw new IllegalStateException("Không thể đặt hàng với nhà cung cấp đang ở trạng thái ngừng hoạt động");
        }

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .createdById(getCurrentUser().getId())
                .supplier(supplier)
                .warehouseId(request.warehouseId())
                .poDate(LocalDateTime.now())
                .deliveryDate(request.deliveryDate())
                .currency(request.currency())
                .taxPercentage(request.taxPercentage() != null
                        ? request.taxPercentage() : new BigDecimal("10.00"))
                .shippingCost(request.shippingCost() != null
                        ? request.shippingCost() : BigDecimal.ZERO)
                .discountAmount(request.discountAmount() != null
                        ? request.discountAmount() : BigDecimal.ZERO)
                .paymentTerms(request.paymentTerms())
                .incoterms(request.incoterms())
                .notes(request.notes())
                .status(PurchaseOrderStatus.DRAFT)
                .build();

        int lineNumber = 1;
        for (PurchaseOrderItemRequest itemRequest : request.items()) {
            PurchaseOrderItem item = purchaseOrderMapper.toItemEntity(itemRequest);
            item.setLineNumber(lineNumber++);
            purchaseOrder.addItem(item);
        }

        recalculateTotals(purchaseOrder);

        PurchaseOrder saved = purchaseOrderRepository.save(purchaseOrder);
        return purchaseOrderMapper.toDetailResponse(saved);
    }

    private void recalculateTotals(PurchaseOrder purchaseOrder) {
        BigDecimal subtotal = purchaseOrder.getItems().stream()
                .map(PurchaseOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal taxAmount = subtotal
                .multiply(purchaseOrder.getTaxPercentage())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal
                .add(taxAmount)
                .add(purchaseOrder.getShippingCost())
                .subtract(purchaseOrder.getDiscountAmount())
                .setScale(4, RoundingMode.HALF_UP);

        purchaseOrder.setSubtotal(subtotal);
        purchaseOrder.setTaxAmount(taxAmount);
        purchaseOrder.setGrandTotal(grandTotal);
    }

    private String generatePoNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequenceValue = sequenceRepository.nextPoNumberSequence();
        return "PO-" + datePart + "-" + String.format("%06d", sequenceValue);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse update(UUID id, UpdatePurchaseOrderRequest request) {
        log.info("Bắt đầu cập nhật đơn đặt hàng id={}", id);
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ có thể sửa đơn đặt hàng khi đang ở trạng thái DRAFT, hiện tại: "
                            + purchaseOrder.getStatus());

        }
        purchaseOrder.setDeliveryDate(request.deliveryDate());
        purchaseOrder.setTaxPercentage(request.taxPercentage() != null
                ? request.taxPercentage() : purchaseOrder.getTaxPercentage());
        purchaseOrder.setShippingCost(request.shippingCost() != null
                ? request.shippingCost() : purchaseOrder.getShippingCost());
        purchaseOrder.setDiscountAmount(request.discountAmount() != null
                ? request.discountAmount() : purchaseOrder.getDiscountAmount());
        purchaseOrder.setPaymentTerms(request.paymentTerms());
        purchaseOrder.setIncoterms(request.incoterms());
        purchaseOrder.setNotes(request.notes());


        recalculateTotals(purchaseOrder);
        log.info("Cập nhật đơn đặt hàng thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public PurchaseOrderDetailResponse getById(UUID id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt hàng"));

        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> getAll(Pageable pageable) {
        return purchaseOrderRepository.findByIsDeletedFalse(pageable)
                .map(purchaseOrderMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<PurchaseOrderSummaryResponse> search(
            PurchaseOrderSearchRequest searchRequest, Pageable pageable) {

        LocalDateTime fromDateTime = searchRequest.fromDate() != null
                ? searchRequest.fromDate().atStartOfDay()
                : null;
        LocalDateTime toDateTime = searchRequest.toDate() != null
                ? searchRequest.toDate().atTime(LocalTime.MAX)
                : null;

        return purchaseOrderRepository.searchPurchaseOrders(
                        searchRequest.supplierId(),
                        searchRequest.status(),
                        fromDateTime,
                        toDateTime,
                        pageable)
                .map(purchaseOrderMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse addItem(UUID poId, AddPurchaseOrderItemRequest request) {
        log.info("Thêm dòng sản phẩm vào đơn đặt hàng id={}", poId);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(poId);
        validateDraftForItemModification(purchaseOrder);

        PurchaseOrderItem item = purchaseOrderMapper.toItemEntity(request);

        int nextLineNumber = purchaseOrder.getItems().stream()
                .mapToInt(PurchaseOrderItem::getLineNumber)
                .max()
                .orElse(0) + 1;
        item.setLineNumber(nextLineNumber);

        purchaseOrder.addItem(item);
        recalculateTotals(purchaseOrder);

        log.info("Thêm dòng sản phẩm thành công vào PO id={}, lineNumber={}", poId, nextLineNumber);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse updateItem(UUID poId, UUID itemId, UpdatePurchaseOrderItemRequest request) {
        log.info("Cập nhật dòng sản phẩm id={} trong đơn đặt hàng id={}", itemId, poId);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(poId);
        validateDraftForItemModification(purchaseOrder);

        PurchaseOrderItem item = purchaseOrder.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy dòng sản phẩm id=" + itemId + " trong đơn đặt hàng " + poId));

        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setDescription(request.description());

        recalculateTotals(purchaseOrder);

        log.info("Cập nhật dòng sản phẩm thành công, itemId={}", itemId);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    public PurchaseOrderDetailResponse removeItem(UUID poId, UUID itemId) {
        log.info("Xóa dòng sản phẩm id={} khỏi đơn đặt hàng id={}", itemId, poId);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(poId);
        validateDraftForItemModification(purchaseOrder);

        if (purchaseOrder.getItems().size() <= 1) {
            throw new IllegalStateException(
                    "Không thể xóa - đơn đặt hàng phải có ít nhất 1 sản phẩm");
        }

        PurchaseOrderItem item = purchaseOrder.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy dòng sản phẩm id=" + itemId + " trong đơn đặt hàng " + poId));

        purchaseOrder.removeItem(item);
        recalculateTotals(purchaseOrder);

        log.info("Xóa dòng sản phẩm thành công, itemId={}", itemId);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse submitForApproval(UUID id) {
        log.info("Gửi duyệt đơn đặt hàng id={}", id);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.PENDING_APPROVAL);

        if (purchaseOrder.getItems().isEmpty()) {
            throw new IllegalStateException("Không thể gửi duyệt đơn hàng không có sản phẩm nào");
        }

        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);

        log.info("Gửi duyệt thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse approve(UUID id, ApprovePurchaseOrderRequest request) {
        log.info("Duyệt đơn đặt hàng id={}", id);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.APPROVED);

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovedById(getCurrentUser().getId());
        purchaseOrder.setApprovalDate(LocalDateTime.now());

        log.info("Duyệt đơn đặt hàng thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse reject(UUID id, RejectPurchaseOrderRequest request) {

        log.info("Từ chối đơn đặt hàng id={}, lý do={}", id, request.reason());

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.REJECTED);

        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);
        purchaseOrder.setRejectionReason(request.reason());

        log.info("Từ chối đơn đặt hàng thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse sendToSupplier(UUID id) {
        log.info("Gửi đơn đặt hàng cho nhà cung cấp, id={}", id);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.SENT_TO_SUPPLIER);

        purchaseOrder.setStatus(PurchaseOrderStatus.SENT_TO_SUPPLIER);

        log.info("Gửi đơn đặt hàng cho NCC thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse cancel(UUID id, CancelPurchaseOrderRequest request) {
        log.info("Hủy đơn đặt hàng id={}, lý do={}", id, request.reason());

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.CANCELLED);

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrder.setCancelledById(getCurrentUser().getId());
        purchaseOrder.setCancelledAt(LocalDateTime.now());
        purchaseOrder.setCancellationReason(request.reason());

        log.info("Hủy đơn đặt hàng thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse close(UUID id) {
        log.info("Đóng đơn đặt hàng id={}", id);

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.CLOSED);

        purchaseOrder.setStatus(PurchaseOrderStatus.CLOSED);

        log.info("Đóng đơn đặt hàng thành công, id={}", id);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Bắt đầu xoá (soft delete) đơn đặt hàng id={}", id);

        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn đặt hàng với id: " + id));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ có thể xoá đơn đặt hàng khi đang ở trạng thái DRAFT");
        }

        purchaseOrder.setIsDeleted(true);

        log.info("Xoá (soft delete) đơn đặt hàng thành công, id={}", id);
    }

    private void validateDraftForItemModification(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ có thể thêm/sửa/xóa sản phẩm khi đơn đặt hàng đang ở trạng thái DRAFT, hiện tại: "
                            + purchaseOrder.getStatus());
        }
    }

    private PurchaseOrder getForUpdateOrThrow(UUID id) {
        return purchaseOrderRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt hàng"));
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
