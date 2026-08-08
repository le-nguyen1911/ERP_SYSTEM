package com.ERP_SYSTEM.sales.service.Impl;

import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.purchase.repository.SequenceRepository;
import com.ERP_SYSTEM.sales.dto.request.*;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderDetailResponse;
import com.ERP_SYSTEM.sales.dto.response.SalesOrderSummaryResponse;
import com.ERP_SYSTEM.sales.entity.Customer;
import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import com.ERP_SYSTEM.sales.entity.SalesOrder;
import com.ERP_SYSTEM.sales.entity.SalesOrderItem;
import com.ERP_SYSTEM.sales.mapper.SalesOrderMapper;
import com.ERP_SYSTEM.sales.repository.CustomerRepository;
import com.ERP_SYSTEM.sales.repository.SalesOrderRepository;
import com.ERP_SYSTEM.sales.service.SalesOrderService;
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
import java.time.format.DateTimeFormatter;
import java.util.*;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class SalesOrderServiceImpl implements SalesOrderService {
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerRepository customerRepository;
    private final SalesOrderMapper salesOrderMapper;
    private final SequenceRepository sequenceRepository;
    private final UserRepository userRepository;

    private static final Map<SalesOrderStatus, Set<SalesOrderStatus>> VALID_TRANSITIONS =
            new EnumMap<>(SalesOrderStatus.class);

    static {
        VALID_TRANSITIONS.put(SalesOrderStatus.DRAFT,
                EnumSet.of(SalesOrderStatus.PENDING_APPROVAL, SalesOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(SalesOrderStatus.PENDING_APPROVAL,
                EnumSet.of(SalesOrderStatus.APPROVED, SalesOrderStatus.REJECTED));
        VALID_TRANSITIONS.put(SalesOrderStatus.APPROVED,
                EnumSet.of(SalesOrderStatus.CONFIRMED, SalesOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(SalesOrderStatus.CONFIRMED,
                EnumSet.of(SalesOrderStatus.DELIVERED, SalesOrderStatus.CANCELLED));
        VALID_TRANSITIONS.put(SalesOrderStatus.DELIVERED,
                EnumSet.of(SalesOrderStatus.CLOSED));
    }

    private void validateTransition(SalesOrderStatus from, SalesOrderStatus to) {
        Set<SalesOrderStatus> allowed = VALID_TRANSITIONS.getOrDefault(from, Set.of());
        if (!allowed.contains(to)) {
            throw new IllegalStateException(
                    "Không thể chuyển đơn hàng từ trạng thái " + from + " sang " + to);
        }
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse create(CreateSalesOrderRequest request) {
        log.info("Bắt đầu tạo đơn bán hàng cho customer={}", request.customerId());
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(request.customerId())
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy khách hàng"));

        if (customer.getStatus() != CustomerStatus.ACTIVE) {
            throw new IllegalStateException("Khách hàng đang ở trạng thái INACTIVE Không thể bán hàng");
        }

        SalesOrder salesOrder = SalesOrder.builder()
                .soNumber(generateSoNumber())
                .customer(customer)
                .warehouseId(request.warehouseId())
                .soDate(LocalDateTime.now())
                .deliveryDate(request.deliveryDate())
                .currency(request.currency())
                .taxPercentage(request.taxPercentage() != null ? request.taxPercentage() : new BigDecimal("10.00"))
                .shippingCost(request.shippingCost() != null ? request.shippingCost() : BigDecimal.ZERO)
                .discountAmount(request.discountAmount() != null ? request.discountAmount() : BigDecimal.ZERO)
                .paymentTerms(request.paymentTerms())
                .shippingAddress(request.shippingAddress())
                .notes(request.notes())
                .status(SalesOrderStatus.DRAFT)
                .build();
        int lineNumber = 1;
        for (SalesOrderItemRequest itemRequest : request.items()) {
            SalesOrderItem item = salesOrderMapper.toItemEntity(itemRequest);
            item.setLineNumber(lineNumber++);
            salesOrder.addItem(item);
        }
        recalculateTotals(salesOrder);
        SalesOrder saved = salesOrderRepository.save(salesOrder);
        log.info("Tạo đơn bán hàng thành công, id={}, soNumber={}", saved.getId(), salesOrder.getSoNumber());
        return salesOrderMapper.toDetailResponse(saved);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse update(UUID id, UpdateSalesOrderRequest request) {
        log.info("Bắt đầu cập nhật đơn bán hàng với id={}", id);

        SalesOrder salesOrder = salesOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn bán hàng với id: " + id));

        if (salesOrder.getStatus() != SalesOrderStatus.DRAFT) {
            throw new IllegalStateException("chỉ có thể chỉnh sưa đơn bán hnahf ở trạng thái DRAFT" + salesOrder.getStatus());
        }

        salesOrder.setDeliveryDate(request.deliveryDate());
        salesOrder.setTaxPercentage(request.taxPercentage() != null
                ? request.taxPercentage() : salesOrder.getTaxPercentage());
        salesOrder.setShippingCost(request.shippingCost() != null
                ? request.shippingCost() : salesOrder.getShippingCost());
        salesOrder.setDiscountAmount(request.discountAmount() != null
                ? request.discountAmount() : salesOrder.getDiscountAmount());
        salesOrder.setPaymentTerms(request.paymentTerms());
        salesOrder.setShippingAddress(request.shippingAddress());
        salesOrder.setNotes(request.notes());

        recalculateTotals(salesOrder);
        log.info("Cập nhật thánh công , id={}", id);

        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    public SalesOrderDetailResponse getById(UUID id) {
        SalesOrder salesOrder = salesOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn bán hàng với id: " + id));
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    public Page<SalesOrderSummaryResponse> getAll(Pageable pageable) {
        return salesOrderRepository.findByIsDeletedFalse(pageable)
                .map(salesOrderMapper::toSummaryResponse);
    }

    @Override
    public Page<SalesOrderSummaryResponse> search(SalesOrderSearchRequest request, Pageable pageable) {
        return salesOrderRepository.searchSalesOrders(request.customerId(),
                        request.status(),
                        request.fromDate(),
                        request.toDate(),
                        pageable)
                .map(salesOrderMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse addItem(UUID soId, AddSalesOrderItemRequest request) {
        log.info("Thêm dòng sản phẩm vào đơn bán hàng id={}", soId);
        SalesOrder salesOrder = getOrThrow(soId);
        validateDraftForItemModification(salesOrder);
        SalesOrderItem item = salesOrderMapper.toItemEntity(request);
        int nextLineNumber = salesOrder.getItems().stream()
                .mapToInt(SalesOrderItem::getLineNumber)
                .max()
                .orElse(0) + 1;
        item.setLineNumber(nextLineNumber);
        salesOrder.addItem(item);
        recalculateTotals(salesOrder);
        log.info("Thêm dòng sản phẩm vào đơn hàng thành công soId={}, lineNumber={}", soId, nextLineNumber);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse updateItem(UUID soId, UUID itemId, UpdateSalesOrderItemRequest request) {
        log.info("Cập nhật dòng sản phẩm id={} trong đơn bán hàng id={}", itemId, soId);
        SalesOrder salesOrder = getOrThrow(soId);
        validateDraftForItemModification(salesOrder);
        SalesOrderItem item = salesOrder.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng sản phẩm"));
        item.setQuantity(request.quantity());
        item.setUnitPrice(request.unitPrice());
        item.setDescription(request.description());

        recalculateTotals(salesOrder);
        log.info("Cập nhật dòng sản phẩm thành công itemId={}", itemId);

        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse removeItem(UUID soId, UUID itemId) {
        log.info("Xóa dòng sản phẩm id={} khỏi đơn bán hàng id={}", itemId, soId);
        SalesOrder salesOrder = getOrThrow(soId);
        validateDraftForItemModification(salesOrder);

        if (salesOrder.getItems().size() <= 1) {
            throw new IllegalStateException("Đơn bán hàng ít nhất 1 sản phẩm");
        }
        SalesOrderItem item = salesOrder.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy dòng sản phẩm"));
        salesOrder.removeItem(item);
        recalculateTotals(salesOrder);
        log.info("Xoá thành công dòng sản phẩm , itemId={}", itemId);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse submitForApproval(UUID id) {
        log.info("Gửi duyệt đơn bán hàng id={}", id);
        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.PENDING_APPROVAL);

        if (salesOrder.getItems().isEmpty()) {
            throw new IllegalStateException("Không thể gửi duyệt đơn hàng không có sản phẩm");
        }
        salesOrder.setStatus(SalesOrderStatus.PENDING_APPROVAL);
        log.info("Gửi duyển đơn hàng thành công, id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse approve(UUID id, ApproveSalesOrderRequest request) {
        log.info("Phê duyệt đơn bán hàng id={}", id);
        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.APPROVED);
        salesOrder.setStatus(SalesOrderStatus.APPROVED);
        salesOrder.setApprovedById(getCurrentUser().getId());
        salesOrder.setApprovalDate(LocalDateTime.now());
        log.info("Phê duyệt đơn bán hàng thành công, id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse reject(UUID id, RejectSalesOrderRequest request) {
        log.info("Từ chối đơn bán hàng id={}", id);
        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.REJECTED);
        salesOrder.setStatus(SalesOrderStatus.REJECTED);
        salesOrder.setRejectionReason(request.reason());
        log.info("Đơn bán hàng đã bị từ chối , id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse confirm(UUID id) {
        log.info("Xác nhận đơn bán hàng với id={}", id);
        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.CONFIRMED);
        salesOrder.setStatus(SalesOrderStatus.CONFIRMED);
        log.info("Đơn bán hàng đã được xác nhận , id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse cancel(UUID id, CancelSalesOrderRequest request) {
        log.info("Huỷ đơn bán hàng id={} , với lý do ={}", id, request.reason());
        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.CANCELLED);
        salesOrder.setStatus(SalesOrderStatus.CANCELLED);
        salesOrder.setCancellationReason(request.reason());
        salesOrder.setCancelledById(getCurrentUser().getId());
        salesOrder.setCancelledAt(LocalDateTime.now());
        log.info("Đã huỷ đơn hàng thành công , id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public SalesOrderDetailResponse close(UUID id) {
        log.info("Đóng đơn bán hàng id={}", id);

        SalesOrder salesOrder = getOrThrow(id);
        validateTransition(salesOrder.getStatus(), SalesOrderStatus.CLOSED);

        salesOrder.setStatus(SalesOrderStatus.CLOSED);

        log.info("Đóng đơn bán hàng thành công, id={}", id);
        return salesOrderMapper.toDetailResponse(salesOrder);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Xoá Đơn bán hàng với id={}", id);
        SalesOrder salesOrder = salesOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm tháy đơn bán hàng với id={}", id));

        if (salesOrder.getStatus().equals(SalesOrderStatus.DRAFT)) {
            throw new IllegalStateException("cHỈ thể xoá đơn bán hàng Ở trạng thái DRAFT ");
        }

        salesOrder.setIsDeleted(true);
        log.info("Xoá (soft delete) đơn bán hàng thành công, id={}", id);
    }


    // helper method
    private void validateDraftForItemModification(SalesOrder salesOrder) {
        if (salesOrder.getStatus() != SalesOrderStatus.DRAFT) {
            throw new IllegalStateException(
                    "Chỉ có thể thêm/sửa/xóa sản phẩm khi đơn bán hàng đang ở trạng thái DRAFT, hiện tại: "
                            + salesOrder.getStatus());
        }
    }

    private String generateSoNumber() {
        String datePart = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        Long sequenceValue = sequenceRepository.nextSoNumberSequence();
        return "SO-" + datePart + "-" + String.format("%06d", sequenceValue);
    }

    private void recalculateTotals(SalesOrder salesOrder) {
        BigDecimal subtotal = salesOrder.getItems().stream()
                .map(SalesOrderItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(4, RoundingMode.HALF_UP);

        BigDecimal taxAmount = subtotal
                .multiply(salesOrder.getTaxPercentage())
                .divide(new BigDecimal("100"), 4, RoundingMode.HALF_UP);

        BigDecimal grandTotal = subtotal
                .add(taxAmount)
                .add(salesOrder.getShippingCost())
                .subtract(salesOrder.getDiscountAmount())
                .setScale(4, RoundingMode.HALF_UP);

        salesOrder.setSubtotal(subtotal);
        salesOrder.setTaxAmount(taxAmount);
        salesOrder.setGrandTotal(grandTotal);
    }

    private SalesOrder getOrThrow(UUID id) {
        return salesOrderRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy đơn bán hàng với id: " + id));
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
