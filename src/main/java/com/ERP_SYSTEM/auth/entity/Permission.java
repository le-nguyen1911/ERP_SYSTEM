package com.ERP_SYSTEM.auth.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;
@Entity
@Table(name = "permissions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission extends BaseEntity {
    @Column(nullable = false, unique = true)
    private String name;
    private String description;

    @Builder.Default
    @ManyToMany(mappedBy = "permissions", fetch = FetchType.LAZY)

    private Set<Role> roles = new HashSet<>();
}
