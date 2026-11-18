package com.ERP_SYSTEM.auth.controller;

import com.ERP_SYSTEM.auth.dto.request.AssignRoleRequest;
import com.ERP_SYSTEM.auth.dto.request.ChangePasswordRequest;
import com.ERP_SYSTEM.auth.dto.request.UpdateUserRequest;
import com.ERP_SYSTEM.auth.dto.response.UserInfoResponse;
import com.ERP_SYSTEM.auth.service.UserService;
import com.ERP_SYSTEM.common.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@Controller
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    /**
     * GET /api/v1/users
     * Returns a page of users filtered by privilege:
     * - SUPER_ADMIN: sees all users.
     * - ADMIN: sees all users except those with SUPER_ADMIN role.
     */
    @GetMapping
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Page<UserInfoResponse>>> getAllUsers(
            @PageableDefault(
                    size = 10,
                    sort = "createdAt",
                    direction = Sort.Direction.DESC
            ) Pageable pageable
    ) {
        return ResponseEntity.ok(
                ApiResponse.success(userService.getAllUsersForCaller(pageable))
        );
    }

    /**
     * GET /api/v1/users/{id}
     * ADMIN is blocked from reading a SUPER_ADMIN user by the service layer (403).
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_VIEW') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> getUserById(@PathVariable UUID id) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        userService.getUserById(id)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_UPDATE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> updateUserById(@PathVariable UUID id, @Valid @RequestBody UpdateUserRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Cập nhật thông ttin thành công",
                        userService.updateUser(id, request)
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('USER_DELETE') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> deleteUserById(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Xoá User thành công",
                        null
                )
        );
    }

    @PatchMapping("/{id}/lock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> lockUser(@PathVariable UUID id) {
        userService.lockUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Khoá tài khoaản thành công", null
                )
        );
    }

    @PatchMapping("/{id}/unlock")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> unlockUser(@PathVariable UUID id) {
        userService.unlockUser(id);
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Mở khoá tài khoaản thành công", null
                )
        );
    }

    @PostMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserInfoResponse>> assignRoles(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {
        return ResponseEntity.ok(
                ApiResponse.success(
                        "Gán role thành công",
                        userService.assignRoles(id, request))
        );
    }

    @DeleteMapping("/{id}/roles")
    @PreAuthorize("hasRole('ADMIN') or hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Object>> removeRoles(@PathVariable UUID id, @Valid @RequestBody AssignRoleRequest request) {

        return ResponseEntity.ok(
                ApiResponse.success("Xoá role khỏi user thành công", userService.removeRoles(id, request))
        );
    }


    @PutMapping("/me/password")
    public ResponseEntity<ApiResponse<Object>> changePassword(@AuthenticationPrincipal UserDetails userDetails, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok(
                ApiResponse.success("Đổi mật khẩu thành công", null)
        );
    }


}
