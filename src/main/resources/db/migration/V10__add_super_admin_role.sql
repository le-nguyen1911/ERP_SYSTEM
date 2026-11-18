-- ============================================================
-- V10: Add SUPER_ADMIN role
-- This migration adds the SUPER_ADMIN role if it does not exist.
-- It does NOT create any user with this role automatically.
-- Assigning the role to a user must be done through a secure
-- out-of-band process (e.g. a trusted admin script or first-run CLI).
-- ============================================================

INSERT INTO roles (id, name, description, created_at, updated_at)
SELECT gen_random_uuid(),
       'SUPER_ADMIN',
       'Quản trị tối cao – toàn quyền hệ thống, bao gồm quản lý ADMIN, MANAGER, USER và tất cả phân hệ nghiệp vụ.',
       NOW(),
       NOW()
WHERE NOT EXISTS (
    SELECT 1 FROM roles WHERE UPPER(name) = 'SUPER_ADMIN'
);

-- Grant ALL permissions to SUPER_ADMIN role
INSERT INTO role_permissions (role_id, permission_id)
SELECT r.id, p.id
FROM roles r
         CROSS JOIN permissions p
WHERE UPPER(r.name) = 'SUPER_ADMIN'
  AND NOT EXISTS (
    SELECT 1
    FROM role_permissions rp
    WHERE rp.role_id = r.id
      AND rp.permission_id = p.id
);
