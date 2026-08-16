package com.ERP_SYSTEM.auth.service.Implement;

import com.ERP_SYSTEM.audit.annotation.Auditable;
import com.ERP_SYSTEM.audit.entity.enums.AuditAction;
import com.ERP_SYSTEM.audit.service.AuditLogService;
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
import com.ERP_SYSTEM.notification.entity.Enum.SourceModule;
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
    private final AuditLogService auditLogService;

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


    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.UPDATE, module = SourceModule.AUTH)
    @Override
    @Transactional
    public UserInfoResponse updateUser(UUID id, UpdateUserRequest request) {
        User user = findUserById(id);
        if (request.getEmail() != null
                && !request.getEmail().equals(user.getEmail())
                && userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email đã được sử dụng");
        }
        userMapper.updateUserFromRequest(request, user);
        userRepository.save(user);

        return userMapper.toUserInfoResponse(user);
    }

    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.DELETE, module = SourceModule.AUTH)
    @Override
    @Transactional
    public void deleteUser(UUID id) {
        User user = findUserById(id);
        userRepository.delete(user);
    }

    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.STATUS_CHANGE, module = SourceModule.AUTH)
    @Override
    @Transactional
    public void lockUser(UUID id) {
        User user = findUserById(id);
        user.setEnabled(false);
        userRepository.save(user);
    }

    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.STATUS_CHANGE, module = SourceModule.AUTH)
    @Override
    @Transactional
    public void unlockUser(UUID id) {
        User user = findUserById(id);
        user.setEnabled(true);
        userRepository.save(user);
    }


    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.UPDATE, module = SourceModule.AUTH)
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

    @Auditable(entityClass = User.class, entityType = "User",
            action = AuditAction.UPDATE, module = SourceModule.AUTH)
    @Override
    @Transactional
    public UserInfoResponse removeRoles(UUID id, AssignRoleRequest request) {
        User user = findUserById(id);
        user.getRoles().removeIf(role -> request.getRoles().contains(role.getName()));
        userRepository.save(user);
        return userMapper.toUserInfoResponse(user);
    }


    @Override
    @Transactional
    public void changePassword(String username, ChangePasswordRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user"));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new RuntimeException("Password cũ không đúng");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        refreshTokenRepository.revokeAllUserTokens(
                user.getId(), LocalDateTime.now(), "PASSWORD_CHANGE");


        auditLogService.logSystemEvent(
                "User", user.getId(), AuditAction.UPDATE, user.getId(),
                SourceModule.AUTH, "Người dùng tự đổi mật khẩu");
    }

    private User findUserById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy user với id: " + id));
    }
}