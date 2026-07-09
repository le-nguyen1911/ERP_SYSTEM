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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SupplierServiceImpl implements SupplierService {
    private final SupplierRepository supplierRepository;
    private final SupplierMapper supplierMapper;
    private final PurchaseOrderRepository purchaseOrderRepository;

    @Override
    @Transactional
    public SupplierResponse create(CreateSupplierRequest request) {
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
        return supplierMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public SupplierResponse update(UUID id, UpdateSupplierRequest request) {
        Supplier supplier = supplierRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy nhà cung cấp"));
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
    public Page<SupplierResponse> search(SupplierSearchRequest request, Pageable pageable) {
        return supplierRepository.searchSuppliers(request.keyword(), request.status(), pageable)
                .map(supplierMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {

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

    }

}
