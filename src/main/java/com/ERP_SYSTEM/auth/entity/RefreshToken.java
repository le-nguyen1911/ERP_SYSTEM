package com.ERP_SYSTEM.auth.entity;

import com.ERP_SYSTEM.common.base.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "refresh_tokens")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;


    @Column(nullable = false)
    private String deviceInfo;


    @Column(nullable = false)
    private LocalDateTime expiresAt;


    @Builder.Default
    private Boolean revoked = false;


    private LocalDateTime revokedAt;


    private String revokedReason;


}
