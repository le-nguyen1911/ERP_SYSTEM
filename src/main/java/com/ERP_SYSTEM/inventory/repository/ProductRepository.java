package com.ERP_SYSTEM.inventory.repository;

import com.ERP_SYSTEM.inventory.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByCode(String code);

    boolean existsByCode(String code);

    Page<Product> findByActiveTrue(Pageable pageable);

    @Query("""
            SELECT p FROM Product p
            WHERE (:keyword IS NULL
                OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
                OR LOWER(p.code) LIKE LOWER(CONCAT('%', :keyword, '%')))
            AND p.active = true
            """)
    Page<Product> searchProducts(
            @Param("keyword") String keyword,
            Pageable pageable);
}
