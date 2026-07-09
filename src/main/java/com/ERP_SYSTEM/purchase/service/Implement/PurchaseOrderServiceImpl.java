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
import com.ERP_SYSTEM.purchase.repository.SupplierRepository;
import com.ERP_SYSTEM.purchase.service.PurchaseOrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Service
@RequiredArgsConstructor
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SupplierRepository supplierRepository;
    private final PurchaseOrderMapper purchaseOrderMapper;
    private final UserRepository userRepository;

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
        // REJECTED, CANCELLED, CLOSED là trạng thái cuối (terminal state)
        // - không có transition nào đi ra từ đây, nên không cần put vào Map
        // (Map.get() trả về null -> validateTransition() sẽ coi Set rỗng).
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
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(request.supplierId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cng cấp"));
        if (supplier.getStatus() != SupplierStatus.ACTIVE) {
            throw new IllegalStateException("Không thể đặt hàng với nhà cung cấp đang ở trạng thái ngừng hoạt động");
        }

        PurchaseOrder purchaseOrder = PurchaseOrder.builder()
                .poNumber(generatePoNumber())
                .supplier(supplier)
                .warehouseId(request.warehouseId())
                .requisitionId(request.requisitionId())
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
        int randomPart = ThreadLocalRandom.current().nextInt(1000, 10000);
        return "PO-" + datePart + "-" + randomPart;
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse update(UUID id, UpdatePurchaseOrderRequest request) {
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
    public Page<PurchaseOrderSummaryResponse> search(PurchaseOrderSearchRequest request, Pageable pageable) {
        return purchaseOrderRepository.searchPurchaseOrders(
                        request.supplierId(),
                        request.status(),
                        request.fromDate(),
                        request.toDate(),
                        pageable
                )
                .map(purchaseOrderMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse submitForApproval(UUID id) {
        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.PENDING_APPROVAL);
        if (purchaseOrder.getItems().isEmpty()) {
            throw new IllegalStateException("Không thể gửi duyệt đơn hàng không có sản phảm");
        }
        purchaseOrder.setStatus(PurchaseOrderStatus.PENDING_APPROVAL);
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse approve(UUID id, ApprovePurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.APPROVED);

        purchaseOrder.setStatus(PurchaseOrderStatus.APPROVED);
        purchaseOrder.setApprovalDate(LocalDateTime.now());
        purchaseOrder.setApprovedById(getCurrentUser().getId());
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse reject(UUID id, RejectPurchaseOrderRequest request) {
        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.REJECTED);
        purchaseOrder.setStatus(PurchaseOrderStatus.REJECTED);
        purchaseOrder.setCancellationReason(request.reason());
        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse sendToSupplier(UUID id) {
        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.SENT_TO_SUPPLIER);

        purchaseOrder.setStatus(PurchaseOrderStatus.SENT_TO_SUPPLIER);

        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse cancel(UUID id, CancelPurchaseOrderRequest request) {

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.CANCELLED);

        purchaseOrder.setStatus(PurchaseOrderStatus.CANCELLED);
        purchaseOrder.setCancelledById(getCurrentUser().getId());
        purchaseOrder.setCancelledAt(LocalDateTime.now());
        purchaseOrder.setCancellationReason(request.reason());

        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public PurchaseOrderDetailResponse close(UUID id) {

        PurchaseOrder purchaseOrder = getForUpdateOrThrow(id);
        validateTransition(purchaseOrder.getStatus(), PurchaseOrderStatus.CLOSED);

        purchaseOrder.setStatus(PurchaseOrderStatus.CLOSED);

        return purchaseOrderMapper.toDetailResponse(purchaseOrder);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        PurchaseOrder purchaseOrder = purchaseOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn đặt hàng"));

        if (purchaseOrder.getStatus() != PurchaseOrderStatus.DRAFT) {
            throw new IllegalStateException("Chỉ có thể xoá những đơn hàng đnag ở trạng thái nháp");
        }
        purchaseOrder.setIsDeleted(true);
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
