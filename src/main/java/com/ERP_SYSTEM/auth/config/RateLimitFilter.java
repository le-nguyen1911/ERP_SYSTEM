package com.ERP_SYSTEM.auth.config;

import com.ERP_SYSTEM.common.response.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private static final String LOGIN_PATH = "/api/v1/auth/login";
    private static final String REFRESH_PATH_1 = "/api/v1/auth/refresh-token";
    private static final String REFRESH_PATH_2 = "/api/v1/auth/refresh";
    private static final String REGISTER_PATH = "/api/v1/auth/register";
    private static final String FORGOT_PASSWORD_PATH = "/api/v1/auth/forgot-password";
    private static final String RESET_PASSWORD_PATH = "/api/v1/auth/reset-password";

    private final ObjectMapper objectMapper;

    private final Map<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> refreshTokenBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> registerBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> forgotPasswordBuckets = new ConcurrentHashMap<>();
    private final Map<String, Bucket> resetPasswordBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper != null
                ? objectMapper.copy().registerModule(new JavaTimeModule())
                : new ObjectMapper().registerModule(new JavaTimeModule());
    }

    public RateLimitFilter() {
        this(new ObjectMapper().registerModule(new JavaTimeModule()));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String method = request.getMethod();
        String path = resolvePath(request);
        String clientIp = extractClientIp(request);

        RateLimitTarget target = resolveRateLimitTarget(method, path, clientIp);
        if (target == null) {
            filterChain.doFilter(request, response);
            return;
        }

        ConsumptionProbe probe = target.bucket().tryConsumeAndReturnRemaining(1);
        long limit = target.limit();

        if (probe.isConsumed()) {
            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", String.valueOf(probe.getRemainingTokens()));
            log.debug("Rate limit allowed - IP={}, method={}, path={}, remainingTokens={}",
                    clientIp, method, path, probe.getRemainingTokens());
            filterChain.doFilter(request, response);
        } else {
            long nanosToWaitForRefill = probe.getNanosToWaitForRefill();
            long retryAfterSeconds = (long) Math.ceil((double) nanosToWaitForRefill / 1_000_000_000.0);
            if (retryAfterSeconds <= 0) {
                retryAfterSeconds = 1;
            }

            response.setHeader("X-RateLimit-Limit", String.valueOf(limit));
            response.setHeader("X-RateLimit-Remaining", "0");
            response.setHeader("Retry-After", String.valueOf(retryAfterSeconds));

            log.warn("Rate limit exceeded - IP={}, method={}, path={}, retryAfter={}s",
                    clientIp, method, path, retryAfterSeconds);
            sendTooManyRequestsResponse(response);
        }
    }

    private RateLimitTarget resolveRateLimitTarget(String method, String path, String clientIp) {
        if (!"POST".equalsIgnoreCase(method)) {
            return null;
        }

        if (LOGIN_PATH.equals(path)) {
            Bucket bucket = loginBuckets.computeIfAbsent(clientIp, ip -> RateLimitConfig.newLoginBucket());
            return new RateLimitTarget(bucket, RateLimitConfig.LOGIN_CAPACITY);
        }

        if (REFRESH_PATH_1.equals(path) || REFRESH_PATH_2.equals(path)) {
            Bucket bucket = refreshTokenBuckets.computeIfAbsent(clientIp, ip -> RateLimitConfig.newRefreshTokenBucket());
            return new RateLimitTarget(bucket, RateLimitConfig.REFRESH_CAPACITY);
        }

        if (REGISTER_PATH.equals(path)) {
            Bucket bucket = registerBuckets.computeIfAbsent(clientIp, ip -> RateLimitConfig.newRegisterBucket());
            return new RateLimitTarget(bucket, RateLimitConfig.REGISTER_CAPACITY);
        }

        if (FORGOT_PASSWORD_PATH.equals(path)) {
            Bucket bucket = forgotPasswordBuckets.computeIfAbsent(clientIp, ip -> RateLimitConfig.newForgotPasswordBucket());
            return new RateLimitTarget(bucket, RateLimitConfig.FORGOT_PASSWORD_CAPACITY);
        }

        if (RESET_PASSWORD_PATH.equals(path)) {
            Bucket bucket = resetPasswordBuckets.computeIfAbsent(clientIp, ip -> RateLimitConfig.newResetPasswordBucket());
            return new RateLimitTarget(bucket, RateLimitConfig.RESET_PASSWORD_CAPACITY);
        }

        return null;
    }

    private String resolvePath(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (contextPath != null && !contextPath.isEmpty() && uri.startsWith(contextPath)) {
            uri = uri.substring(contextPath.length());
        }
        if (uri.length() > 1 && uri.endsWith("/")) {
            uri = uri.substring(0, uri.length() - 1);
        }
        return uri;
    }

    private String extractClientIp(HttpServletRequest request) {
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isBlank()) {
            return xForwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequestsResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE + ";charset=UTF-8");

        ApiResponse<Void> apiResponse = ApiResponse.<Void>builder()
                .success(false)
                .message("Quá nhiều yêu cầu. Vui lòng thử lại sau.")
                .error("RATE_LIMIT_EXCEEDED")
                .timestamp(LocalDateTime.now())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }

    public void clearBucketsForTesting() {
        loginBuckets.clear();
        refreshTokenBuckets.clear();
        registerBuckets.clear();
        forgotPasswordBuckets.clear();
        resetPasswordBuckets.clear();
    }

    private record RateLimitTarget(Bucket bucket, long limit) {}
}
