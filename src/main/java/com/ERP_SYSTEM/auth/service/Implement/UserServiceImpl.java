package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.auth.dto.request.AssignRoleRequest;
import com.ERP_SYSTEM.auth.dto.request.ChangePasswordRequest;
import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.entity.User;
import com.ERP_SYSTEM.auth.mapper.UserMapper;
import com.ERP_SYSTEM.auth.repository.RefreshTokenRepository;
import com.ERP_SYSTEM.auth.repository.RoleRepository;
import com.ERP_SYSTEM.auth.repository.UserRepository;
import com.ERP_SYSTEM.auth.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;
    private final RefreshTokenRepository refreshTokenRepository;

    @Override
    public Page<UserInfoResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(userMapper::toUserInfoResponse);
    }

    @Override
    public UserInfoResponse getUserById(UUID id) {
        User user = findUserById(id);
        return userMapper.toUserInfoResponse(user);
    }

    @Override
    @Transactional
    public UserInfoResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserById(id);
        // Kiểm tra email mới có bị trùng không
        if (request.getEmail() != null
                // Chỉ check khi có gửi email mới
                && !request.getEmail().equals(user.getEmail())
                // Không check nếu email giống email cũ
                && userRepository.existsByEmail(request.getEmail())) {
            // Email khác mà đã tồn tại → báo lỗi
            throw new RuntimeException("Email đã được sử dụng");
        }
        userMapper.updateUserFromRequest(request, user);
        //MapStruct update các field khác null
        userRepository.save(user);

        return userMapper.toUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = findUserById(id);
        userRepository.delete(user);

    }

    @Override
    @Transactional

    public void lockUser(UUID id) {
        User user = findUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void unlockUser(UUID id) {
        User user = findUserById(id);
        user.setEnabled(true);
        userRepository.save(user);

    }

    @Override
    @Transactional
    public UserInfoResponse assignRoles(UUID id, AssignRoleRequest request) {
        User user = findUserById(id);
        Set<Role> rolesToAdd = request.getRoles().stream().map(rolename -> roleRepository
                        .findByName(rolename)
                        .orElseThrow(() -> new RuntimeException("Không tìm thấy role" + rolename)))
                .collect(Collectors.toSet());
        user.getRoles().addAll(rolesToAdd);
        userRepository.save(user);

        return userMapper.toUserInfoResponse(user);
    }

    @Override
    @Transactional
    public UserInfoResponse removeRoles(UUID id,
                                        AssignRoleRequest request) {
        User user = findUserById(id);

        user.getRoles().removeIf(role ->
                request.getRoles().contains(role.getName())
        );
        // removeIf → xóa phần tử thỏa điều kiện
        // role → role.getName() có trong danh sách cần xóa không?
        // Ví dụ: xóa ["MANAGER"] → chỉ xóa role MANAGER
        // USER vẫn còn

        userRepository.save(user);
        return userMapper.toUserInfoResponse(user);
    }

    @Override
    @Transactional
    public void changePassword(String username,
                               ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy user"
                ));

        // Kiểm tra password cũ có đúng không
        if (!passwordEncoder.matches(
                request.getOldPassword(),
                // password cũ client gửi lên (plain text)
                user.getPassword())) {
            // password hash đang lưu trong DB
            // BCrypt.matches tự so sánh plain vs hash
            throw new RuntimeException("Password cũ không đúng");
        }

        // Hash password mới rồi lưu
        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(
                user.getId(),
                LocalDateTime.now(),
                "PASSWORD_CHANGE"
        );
    }

    // ── Helper method ────────────────────────────────────
    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException(
                        "Không tìm thấy user với id: " + id
                ));
        // Dùng chung cho nhiều method
        // Tránh copy paste .orElseThrow() ở mọi nơi
    }
}
