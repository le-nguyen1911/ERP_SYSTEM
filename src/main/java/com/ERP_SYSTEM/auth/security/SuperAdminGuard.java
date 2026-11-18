package com.ERP_SYSTEM.auth.security;

import com.ERP_SYSTEM.auth.entity.Role;
import com.ERP_SYSTEM.auth.entity.User;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

/**
 * SuperAdminGuard – utility class centralizing all SUPER_ADMIN protection logic.
 *
 * Rules:
 *  1. Only SUPER_ADMIN can interact with any SUPER_ADMIN resource.
 *  2. ADMIN cannot see, modify, or interact with SUPER_ADMIN roles / users.
 *  3. The SUPER_ADMIN role name is immutable; it cannot be renamed or deleted.
 */
@Component
public class SuperAdminGuard {

    public static final String SUPER_ADMIN_ROLE = "SUPER_ADMIN";
    public static final String ROLE_PREFIX = "ROLE_";
    public static final String ROLE_SUPER_ADMIN = ROLE_PREFIX + SUPER_ADMIN_ROLE;
    public static final String ROLE_ADMIN = ROLE_PREFIX + "ADMIN";

    // ── Caller role helpers ──────────────────────────────────────────

    /** Returns true if the currently authenticated user has SUPER_ADMIN role. */
    public boolean callerIsSuperAdmin() {
        return hasAuthority(ROLE_SUPER_ADMIN);
    }

    /** Returns true if the currently authenticated user has ADMIN role (but NOT SUPER_ADMIN). */
    public boolean callerIsAdmin() {
        return hasAuthority(ROLE_ADMIN);
    }

    private boolean hasAuthority(String authority) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(authority::equals);
    }

    // ── Role-level guards ────────────────────────────────────────────

    /**
     * Called whenever a Role entity is about to be read by a non-SUPER_ADMIN.
     * Returns true if the role should be HIDDEN from the caller.
     */
    public boolean shouldHideRole(String roleName) {
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(roleName)) {
            return !callerIsSuperAdmin();
        }
        return false;
    }

    /**
     * Throws 403 if the caller is not allowed to mutate the given role.
     * - SUPER_ADMIN role can only be mutated by SUPER_ADMIN.
     * - Rename/delete of SUPER_ADMIN is always rejected.
     */
    public void assertCanMutateRole(String roleName) {
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(roleName)) {
            if (!callerIsSuperAdmin()) {
                throw new AccessDeniedException(
                        "Vai trò SUPER_ADMIN được bảo vệ. Chỉ SUPER_ADMIN mới có thể thực hiện thao tác này.");
            }
        }
    }

    /**
     * Throws 403 if caller tries to rename a role to SUPER_ADMIN or
     * rename/delete an existing SUPER_ADMIN.
     */
    public void assertCanRenameRole(String currentName, String newName) {
        // Nobody can rename anything TO SUPER_ADMIN except through seed
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(newName) && !SUPER_ADMIN_ROLE.equalsIgnoreCase(currentName)) {
            throw new AccessDeniedException("Không được phép đặt tên vai trò thành SUPER_ADMIN.");
        }
        // Only SUPER_ADMIN can rename the SUPER_ADMIN role itself
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(currentName) && !callerIsSuperAdmin()) {
            throw new AccessDeniedException(
                    "Vai trò SUPER_ADMIN được bảo vệ. Chỉ SUPER_ADMIN mới có thể thực hiện thao tác này.");
        }
    }

    /**
     * Throws 403 if caller tries to delete the SUPER_ADMIN role.
     */
    public void assertCanDeleteRole(String roleName) {
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(roleName)) {
            throw new AccessDeniedException("Không thể xóa vai trò SUPER_ADMIN khỏi hệ thống.");
        }
    }

    // ── User-level guards ────────────────────────────────────────────

    /**
     * Returns true if the user (identified by their roles) should be hidden
     * from the current caller.
     */
    public boolean shouldHideUser(User user) {
        boolean userIsSuperAdmin = user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(SUPER_ADMIN_ROLE::equalsIgnoreCase);
        if (userIsSuperAdmin) {
            return !callerIsSuperAdmin();
        }
        return false;
    }

    /**
     * Throws 403 if the caller (ADMIN) tries to touch a SUPER_ADMIN user.
     */
    public void assertCanMutateUser(User user) {
        boolean userIsSuperAdmin = user.getRoles().stream()
                .map(Role::getName)
                .anyMatch(SUPER_ADMIN_ROLE::equalsIgnoreCase);
        if (userIsSuperAdmin && !callerIsSuperAdmin()) {
            throw new AccessDeniedException(
                    "Không được phép thao tác với tài khoản có vai trò SUPER_ADMIN.");
        }
    }

    /**
     * Throws 403 if the caller tries to assign SUPER_ADMIN role to any user
     * without being a SUPER_ADMIN themselves.
     */
    public void assertCanAssignRole(String roleName) {
        if (SUPER_ADMIN_ROLE.equalsIgnoreCase(roleName) && !callerIsSuperAdmin()) {
            throw new AccessDeniedException(
                    "Chỉ SUPER_ADMIN mới có thể gán vai trò SUPER_ADMIN cho tài khoản khác.");
        }
    }

    /**
     * Throws 403 if the caller tries to revoke the last SUPER_ADMIN.
     * Pass the current count of users who have the SUPER_ADMIN role (BEFORE removal).
     */
    public void assertLastSuperAdminProtection(long superAdminUserCount) {
        if (superAdminUserCount <= 1) {
            throw new AccessDeniedException(
                    "Không thể xóa SUPER_ADMIN cuối cùng. Hệ thống cần ít nhất một tài khoản SUPER_ADMIN.");
        }
    }
}
