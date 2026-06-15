package com.ERP_SYSTEM.auth.repository;

import com.ERP_SYSTEM.auth.entity.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenRepository
        extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByToken(String token);
    // Tìm token theo giá trị
    // Dùng khi refresh hoặc logout

    List<RefreshToken> findByUserIdAndRevokedFalse(Long userId);
    // Tìm tất cả token hợp lệ của 1 user
    // Dùng để xem user đang đăng nhập trên thiết bị nào

    @Modifying
    // @Modifying → báo Spring đây là câu UPDATE/DELETE
    // Không dùng → Spring tưởng là SELECT → lỗi
    @Query("UPDATE RefreshToken rt SET rt.revoked = true, " +
            "rt.revokedAt = :revokedAt, " +
            "rt.revokedReason = :reason " +
            "WHERE rt.user.id = :userId " +
            "AND rt.revoked = false")
        // Thu hồi TẤT CẢ token của user
        // Dùng khi: đổi mật khẩu, admin thu hồi
    void revokeAllUserTokens(
            @Param("userId") Long userId,
            @Param("revokedAt") LocalDateTime revokedAt,
            @Param("reason") String reason);

    @Modifying
    @Query("DELETE FROM RefreshToken rt " +
            "WHERE rt.expiresAt < :now " +
            "OR rt.revoked = true")
        // Xóa token hết hạn hoặc đã bị thu hồi
        // Dùng để dọn dẹp DB định kỳ
        // Tránh DB phình to theo thời gian
    void deleteExpiredAndRevokedTokens(
            @Param("now") LocalDateTime now);
}