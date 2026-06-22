package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.auth.dto.request.LoginRequest;
import com.ERP_SYSTEM.auth.dto.request.RegisterRequest;
import com.ERP_SYSTEM.auth.dto.response.ActiveSessionResponse;
import com.ERP_SYSTEM.auth.dto.response.AuthResponse;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import com.ERP_SYSTEM.auth.entity.RefreshToken;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.mapper.UserMapper;
import com.ERP_SYSTEM.auth.repository.RefreshTokenRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.auth.security.JwtTokenProvider;
import com.ERP_SYSTEM.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthenticationManager authenticationManager;
    private final UserDetailsServiceImpl userDetailsService;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${jwt.refresh-token-expiration}")
    private long refreshTokenExpiration;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Username đã tồn tại");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã tồn tại tai");
        }
        Role userRole = roleRepository.findByName("USER")
                .orElseThrow(() -> new RuntimeException("Role User chưa tồn tại"));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullname())
                .enabled(true)
                .roles(Set.of(userRole)).build();
        userRepository.save(user);
        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        return buildAuthResponse(userDetails, user, "Web Browser");
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()));

        UserDetails userDetails = userDetailsService.loadUserByUsername(request.getUsername());
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("Không tìm tháy user"));


        return buildAuthResponse(userDetails, user, deviceInfo != null ? deviceInfo : "unlknow device");
    }


    @Override
    @Transactional
    public AuthResponse refreshToken(String tokenValue) {
        RefreshToken refreshToken = (RefreshToken) refreshTokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new RuntimeException("Token không tồn tại"));

        if (Boolean.TRUE.equals(refreshToken.getRevoked())) {
            refreshTokenRepository.revokeAllUserTokens(
                    refreshToken.getUser()
                            .getId(),
                    LocalDateTime.now(),
                    "SUSPICIOUS_ACTIVITY"
            );
            throw new RuntimeException("Refresh token đã bị thu hồi." +
                    "Vui lòng đăng nhập lại");
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Token đã hết hạn ");
        }

        User user = refreshToken.getUser();
        UserDetails userDetails = userDetailsService.loadUserByUsername(user.getUsername());
        if (jwtTokenProvider.isRefreshTokenRotationEnabled()) {
            refreshToken.setRevoked(true);
            refreshToken.setExpiresAt(LocalDateTime.now());
            refreshToken.setRevokedReason("TOKEN_RONATION");
            refreshTokenRepository.save(refreshToken);
        }

        String newAccessToken = jwtTokenProvider.generateAccessToken(userDetails);
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(userDetails);
        saveRefreshToken(user, newRefreshToken, refreshToken.getDeviceInfo());

        return AuthResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .user(userMapper.toUserInfoResponse(user))
                .build();
    }

    @Override
    public UserInfoResponse getMe(String username) {

        //tìm user trong DB
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm  thấy user"));

        //MapStruct tự convert User → UserInfoResponse
        return userMapper.toUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void logout(String TokenValue) {
        RefreshToken refreshToken = refreshTokenRepository
                .findByToken(TokenValue)
                .orElseThrow(() -> new RuntimeException("Token không ồn tại"));

        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshToken.setRevokedReason("LOGOUT");
        refreshTokenRepository.save(refreshToken);
    }

    @Override
    @Transactional
    public void logoutAllDevices(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));
        refreshTokenRepository.revokeAllUserTokens(
                user.getId(),
                LocalDateTime.now(),
                "LOGOUT_ALL_DEVICE"
        );
    }

    @Override
    @Transactional
    public List<ActiveSessionResponse> getActiveSessions(String username, String currentToken) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        return refreshTokenRepository
                .findByUserIdAndRevokedFalse(user.getId())
                .stream()
                .filter(t -> t.getExpiresAt()
                        .isAfter(LocalDateTime.now()))
                .map(t -> ActiveSessionResponse.builder()
                        .id(t.getId())
                        .deviceInfo(t.getDeviceInfo())
                        .createdAt(t.getCreatedAt())
                        .expiresAt(t.getExpiresAt())
                        .current(t.getToken().equals(currentToken))
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void revokeSession(UUID tokenId, String username) {
        RefreshToken refreshToken = refreshTokenRepository.findById(tokenId)
                .orElseThrow(() -> new RuntimeException(
                        "Session không tồn tại"
                ));
        if (!refreshToken.getUser().getUsername().equals(username)) {
            throw new RuntimeException("Không có quyền thu hồi Session này");
        }
        refreshToken.setRevoked(true);
        refreshToken.setRevokedAt(LocalDateTime.now());
        refreshToken.setRevokedReason("REVOKE_BY_USER");
        refreshTokenRepository.save(refreshToken);

    }

    private AuthResponse buildAuthResponse(UserDetails userDetails,
                                           User user, String deviceInfo) {
        String accessToken = jwtTokenProvider.generateAccessToken(userDetails);

        String refreshTokenValue = jwtTokenProvider.generateRefreshTokenValue();

        saveRefreshToken(user, refreshTokenValue, deviceInfo);

        return AuthResponse.builder()
                .accessToken(accessToken)
                // Token ngắn hạn, gửi kèm mỗi API request
                .refreshToken(refreshTokenValue)
                // Token dài hạn, dùng để lấy accessToken mới
                .tokenType("Bearer")
                // Chuẩn OAuth2
                .user(userMapper.toUserInfoResponse(user))
                // Thông tin user trả về cho client hiển thị
                .build();
    }

    private void saveRefreshToken(User user, String refreshTokenValue, String deviceInfo) {
        RefreshToken refreshToken = RefreshToken.builder()
                .token(refreshTokenValue)
                .user(user)
                .deviceInfo(deviceInfo)
                .expiresAt(
                        LocalDateTime.now()
                                .plusSeconds(refreshTokenExpiration / 1000)
                )
                .revoked(false)
                .build();
        refreshTokenRepository.save(refreshToken);

    }
}
