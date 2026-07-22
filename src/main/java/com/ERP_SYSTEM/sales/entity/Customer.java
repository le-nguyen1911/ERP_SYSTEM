package com.ERP_SYSTEM.sales.entity;

import com.ERP_SYSTEM.common.base.SoftDeleteBaseEntity;
import com.ERP_SYSTEM.sales.entity.Enum.CustomerStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "customer")
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Customer extends SoftDeleteBaseEntity {
    @Column(name = "customer_code", nullable = false, unique = true, length = 50)
    private String customerCode;

    @Column(name = "customer_name", nullable = false, length = 255)
    private String customerName;

    @Column(name = "contact_person", nullable = false, length = 100)
    private String contactPerson;

    @Column(name = "email", nullable = false, length = 100)
    private String email;

    @Column(name = "phone", nullable = false, length = 20)
    private String phone;

    @Column(name = "address", nullable = false, columnDefinition = "TEXT")
    private String address;

    @Column(name = "city", length = 100)
    private String city;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "tax_id", length = 50)
    private String taxId;

    @Column(name = "payment_terms", length = 50)
    private String paymentTerms;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    @Builder.Default
    private CustomerStatus status = CustomerStatus.ACTIVE;
}
