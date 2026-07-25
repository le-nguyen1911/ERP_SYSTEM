package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Customer;
import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;
import com.ERP_SYSTEM.sales.entity.Enum.SalesOrderStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, UUID> {
    Optional<Customer> findByIdAndIsDeletedFalse(UUID id);

    Optional<Customer> findByCustomerCodeAndIsDeletedFalse(String code);

    boolean existsByCustomerCodeAndIsDeletedFalse(String code);

    Page<Customer> findByIsDeletedFalse(Pageable pageable);

    Page<Customer> findByStatusAndIsDeletedFalse(CustomerStatus status, Pageable pageable);

    @Query("""
            SELECT c FROM Customer c
            WHERE c.isDeleted = false
            AND (:keyword IS NULL
                 OR LOWER(c.customerName) LIKE LOWER(CONCAT('%', :keyword, '%'))
                 OR LOWER(c.customerCode) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND (:status IS NULL OR c.status = :status)
            """)
    Page<Customer> searchCustomers(
            @Param("keyword") String keyword,
            @Param("status") CustomerStatus status,
            Pageable pageable);


    @Query("""
            SELECT CASE WHEN COUNT(so) > 0 THEN true ELSE false END
            FROM SalesOrder so
            WHERE so.customer.id = :customerId
            AND so.isDeleted = false
            AND so.status NOT IN :excludedStatuses
            """)
    boolean existsActiveByCustomerId(
            @Param("customerId") UUID customerId,
            @Param("excludedStatuses") List<SalesOrderStatus> excludedStatuses);
}
