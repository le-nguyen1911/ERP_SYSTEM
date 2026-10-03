package com.ERP_SYSTEM.auth.config;


import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;

import java.time.Duration;

public class RateLimitConfig {

    public static final long LOGIN_CAPACITY = 5;
    public static final Duration LOGIN_REFILL_DURATION = Duration.ofMinutes(1);

    public static final long REFRESH_CAPACITY = 10;
    public static final Duration REFRESH_REFILL_DURATION = Duration.ofMinutes(1);

    public static final long REGISTER_CAPACITY = 3;
    public static final Duration REGISTER_REFILL_DURATION = Duration.ofMinutes(10);

    public static final long FORGOT_PASSWORD_CAPACITY = 5;
    public static final Duration FORGOT_PASSWORD_REFILL_DURATION = Duration.ofMinutes(10);

    public static final long RESET_PASSWORD_CAPACITY = 5;
    public static final Duration RESET_PASSWORD_REFILL_DURATION = Duration.ofMinutes(10);

    public static Bucket newLoginBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(LOGIN_CAPACITY)
                .refillIntervally(LOGIN_CAPACITY, LOGIN_REFILL_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public static Bucket newRefreshTokenBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REFRESH_CAPACITY)
                .refillIntervally(REFRESH_CAPACITY, REFRESH_REFILL_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public static Bucket newRegisterBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(REGISTER_CAPACITY)
                .refillIntervally(REGISTER_CAPACITY, REGISTER_REFILL_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public static Bucket newForgotPasswordBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(FORGOT_PASSWORD_CAPACITY)
                .refillIntervally(FORGOT_PASSWORD_CAPACITY, FORGOT_PASSWORD_REFILL_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }

    public static Bucket newResetPasswordBucket() {
        Bandwidth limit = Bandwidth.builder()
                .capacity(RESET_PASSWORD_CAPACITY)
                .refillIntervally(RESET_PASSWORD_CAPACITY, RESET_PASSWORD_REFILL_DURATION)
                .build();
        return Bucket.builder().addLimit(limit).build();
    }
}
