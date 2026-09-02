package com.ERP_SYSTEM.auth.security;

import com.ERP_SYSTEM.auth.entity.RefreshToken;
import com.ERP_SYSTEM.auth.repository.RefreshTokenRepository;
import com.ERP_SYSTEM.auth.service.Implement.UserDetailsServiceImpl;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {

        try {
            String token = extractToken(request);
            if (StringUtils.hasText(token) && jwtTokenProvider.validateToken(token)) {
                String tokenType = jwtTokenProvider.getTokenType(token);
                if ("access".equals(tokenType)) {
                    String username = jwtTokenProvider.getUsernameFromToken(token);
                    String sidStr = jwtTokenProvider.getSessionIdFromToken(token);

                    boolean isSessionValid = false;
                    if (StringUtils.hasText(sidStr)) {
                        try {
                            UUID sessionId = UUID.fromString(sidStr);
                            Optional<RefreshToken> sessionOpt = refreshTokenRepository.findByIdWithUser(sessionId);
                            if (sessionOpt.isPresent()) {
                                RefreshToken session = sessionOpt.get();
                                if (!Boolean.TRUE.equals(session.getRevoked())
                                        && session.getExpiresAt().isAfter(LocalDateTime.now())
                                        && session.getUser().getUsername().equals(username)) {
                                    isSessionValid = true;
                                } else {
                                    log.warn("Phiên đăng nhập không hợp lệ hoặc đã bị thu hồi: sid={}, revoked={}",
                                            sessionId, session.getRevoked());
                                }
                            } else {
                                log.warn("Không tìm thấy phiên đăng nhập trong DB: sid={}", sessionId);
                            }
                        } catch (IllegalArgumentException e) {
                            log.warn("Định dạng sessionId không hợp lệ trong JWT: sid={}", sidStr);
                        }
                    } else {
                        log.warn("Access Token thiếu claim sid (phiên đăng nhập)");
                    }

                    if (isSessionValid) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                                userDetails, null, userDetails.getAuthorities());

                        authentication.setDetails(
                                new WebAuthenticationDetailsSource().buildDetails(request)
                        );
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    } else {
                        SecurityContextHolder.clearContext();
                    }
                } else {
                    SecurityContextHolder.clearContext();
                }
            }
        } catch (Exception e) {
            log.error("LỖI XÁC THỰC JWT: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }
        filterChain.doFilter(request, response);

    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (StringUtils.hasText(header) && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
    }
}

