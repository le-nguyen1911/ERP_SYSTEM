package com.ERP_SYSTEM.purchase.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import com.ERP_SYSTEM.purchase.enums.SupplierStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "supplier")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Supplier extends BaseEntity {
    @Column(name = "supplier_code", updatable = false)
    private String supplierCode;

    @Column(name = "supplier_name")
    private String supplierName;

    @Column(name = "contact_person")
    private String contactPerson;

    @Column(name = "email")
    private String email;

    @Column(name = "phone")
    private String phone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "bank_name")
    private String bankName;

    @Column(name = "bank_account_no")
    private String bankAccountNo;

    @Column(name = "bank_account_holder")
    private String bankAccountHolder;

    @Column(name = "payment_terms")
    private String paymentTerms;

    @Column(name = "rating")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierRating rating = SupplierRating.B;

    @Column(name = "tax_id")
    private String taxId;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private SupplierStatus status = SupplierStatus.ACTIVE;

    @Column(name = "is_deleted")
    @Builder.Default
    private Boolean isDeleted = false;

    //helper method
    public boolean isActive() {
        return !isDeleted && status == SupplierStatus.ACTIVE;
    }

    public void delete() {
        this.isDeleted = true;
    }
}
