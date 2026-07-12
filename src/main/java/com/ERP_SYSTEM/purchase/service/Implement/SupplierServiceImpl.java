package com.ERP_SYSTEM.purchase.service.Implement;

import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.purchase.dto.request.CreateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.request.SupplierSearchRequest;
import com.ERP_SYSTEM.purchase.dto.request.UpdateSupplierRequest;
import com.ERP_SYSTEM.purchase.dto.response.SupplierResponse;
import com.ERP_SYSTEM.purchase.entity.Supplier;
import com.ERP_SYSTEM.purchase.enums.PurchaseOrderStatus;
import com.ERP_SYSTEM.purchase.enums.SupplierStatus;
import com.ERP_SYSTEM.purchase.mapper.SupplierMapper;
import com.ERP_SYSTEM.purchase.repository.PurchaseOrderRepository;
import com.ERP_SYSTEM.purchase.repository.SupplierRepository;
import com.ERP_SYSTEM.purchase.service.SupplierService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
        log.info("Bắt đầu tạo nhà cung cấp mới với mã: {}", request.supplierCode());
        if (supplierRepository.existsBySupplierCodeAndIsDeletedFalse(request.supplierCode())) {
            throw new DuplicateResourceException("Nhà cung cấp đã tồn tại");
        }

        Supplier supplier = supplierMapper.toEntity(request);

        if (supplier.getRating() == null) {
            supplier.setRating("B");
        }

        supplier.setStatus(SupplierStatus.ACTIVE);
        supplier.setIsDeleted(false);

        Supplier saved = supplierRepository.save(supplier);
        log.info("Tạo nhà cung cấp thành công, id={}", saved.getId());
        return supplierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SupplierResponse update(UUID id, UpdateSupplierRequest request) {
        log.info("Bắt đầu cập nhật nhà cung cấp id={}", id);
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
        log.info("Cập nhật nhà cung cấp thành công, id={}", id);
        supplierMapper.updateEntityFromRequest(request, supplier);
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public SupplierResponse getById(UUID id) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
        return supplierMapper.toResponse(supplier);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> getAll(Pageable pageable) {
        return supplierRepository.findByIsDeletedFalse(pageable)
                .map(supplierMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<SupplierResponse> search(SupplierSearchRequest searchRequest, Pageable pageable) {
        return supplierRepository.searchSuppliers(
                        searchRequest.keyword(),
                        searchRequest.status(),
                        pageable)
                .map(supplierMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Bắt đầu xoá (soft delete) nhà cung cấp id={}", id);
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy nhà cung cấp với id: " + id));

        List<PurchaseOrderStatus> excludedStatuses = List.of(
                PurchaseOrderStatus.CLOSED,
                PurchaseOrderStatus.CANCELLED,
                PurchaseOrderStatus.REJECTED);

        boolean hasActivePurchaseOrders =
                supplierRepository.existsActiveBySupplierId(id, excludedStatuses);

        if (hasActivePurchaseOrders) {
            throw new IllegalStateException(
                    "Không thể xoá nhà cung cấp đang có đơn đặt hàng chưa hoàn tất");
        }

        supplier.setIsDeleted(true);
        supplier.setStatus(SupplierStatus.INACTIVE);
        log.info("Xoá (soft delete) nhà cung cấp thành công, id={}", id);
    }

}
