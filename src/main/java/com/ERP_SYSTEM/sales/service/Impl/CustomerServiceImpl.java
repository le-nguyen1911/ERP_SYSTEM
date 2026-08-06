package com.ERP_SYSTEM.sales.service.Impl;

import com.ERP_SYSTEM.common.exception.DuplicateResourceException;
import com.ERP_SYSTEM.common.exception.ResourceNotFoundException;
import com.ERP_SYSTEM.sales.dto.request.CreateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.request.CustomerSearchRequest;
import com.ERP_SYSTEM.sales.dto.request.UpdateCustomerRequest;
import com.ERP_SYSTEM.sales.dto.response.CustomerResponse;
import com.ERP_SYSTEM.sales.entity.Customer;
import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import com.ERP_SYSTEM.sales.mapper.CustomerMapper;
import com.ERP_SYSTEM.sales.repository.CustomerRepository;
import com.ERP_SYSTEM.sales.repository.SalesOrderRepository;
import com.ERP_SYSTEM.sales.service.CustomerService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@Transactional
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {
    private final CustomerRepository customerRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerMapper customerMapper;


    @Override
    @Transactional
    public CustomerResponse create(CreateCustomerRequest request) {
        log.info("Bắt đầu tạo khách hàng mới với mã: {}", request.customerCode());

        if (customerRepository.existsByCustomerCodeAndIsDeletedFalse(request.customerCode())) {
            throw new DuplicateResourceException(
                    "Mã khách hàng '" + request.customerCode() + "' đã tồn tại");
        }
        Customer customer = customerMapper.toEntity(request);
        customer.setStatus(CustomerStatus.ACTIVE);
        customer.setIsDeleted(false);
        Customer saved = customerRepository.save(customer);
        log.info("Tạo khách hàng thành công, id={}", saved.getId());
        return customerMapper.toResponse(saved);

    }

    @Override
    @Transactional
    public CustomerResponse update(UUID id, UpdateCustomerRequest request) {
        log.info("Bắt đầu cập nhật khách hàng id={}", id);

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khách hàng với id: " + id));

        customerMapper.updateEntityFromRequest(request, customer);

        log.info("Cập nhật khách hàng thành công, id={}", id);
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public CustomerResponse findById(UUID id) {
        Customer customer = customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khách hàng với id: " + id));
        return customerMapper.toResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> getAll(Pageable pageable) {
        return customerRepository.findByIsDeletedFalse(pageable)
                .map(customerMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustomerResponse> search(CustomerSearchRequest request, Pageable pageable) {

        return customerRepository.searchCustomers(
                        request.keyword(),
                        request.status(),
                        pageable
                )
                .map(customerMapper::toResponse);
    }

    @Override
    @Transactional
    public void delete(UUID id) {
        log.info("Bắt đầu xoá (soft delete) khách hàng id={}", id);

        Customer customer = customerRepository.findByIdAndIsDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Không tìm thấy khách hàng với id: " + id));

        List<SalesOrderStatus> excludedStatuses = List.of(
                SalesOrderStatus.CLOSED,
                SalesOrderStatus.CANCELLED,
                SalesOrderStatus.REJECTED);

        boolean hasActiveSalesOrders =
                customerRepository.existsActiveByCustomerId(id, excludedStatuses);

        if (hasActiveSalesOrders) {
            throw new IllegalStateException(
                    "Không thể xoá khách hàng đang có đơn bán hàng chưa hoàn tất");
        }

        customer.setIsDeleted(true);
        customer.setStatus(CustomerStatus.INACTIVE);

        log.info("Xoá (soft delete) khách hàng thành công, id={}", id);
    }
}
