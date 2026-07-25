package com.ERP_SYSTEM.sales.repository;

import com.ERP_SYSTEM.sales.entity.Delivery;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface DeliveryRepository extends JpaRepository<Delivery, UUID> {
}
