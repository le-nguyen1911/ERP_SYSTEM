package com.ERP_SYSTEM.auth.config;

import com.ERP_SYSTEM.auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class TokenCleanupTask {

    private final RefreshTokenRepository refreshTokenRepository;

    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void cleanupExpiredTokens() {
        log.info("Bắt đầu dọn dẹp token hết hạn...");
        refreshTokenRepository
                .deleteExpiredAndRevokedTokens(LocalDateTime.now());
        log.info("Dọn dẹp token hoàn thành");
    }
}