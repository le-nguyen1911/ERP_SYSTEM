-- V1__create_auth_tables.sql
-- Tạo tất cả bảng của Auth module

-- =============================================
-- BẢNG ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS roles (
                                     id          BIGSERIAL PRIMARY KEY,
    -- BIGSERIAL = BIGINT + AUTO INCREMENT
    -- Tương đương @GeneratedValue(IDENTITY) trong Java

                                     name        VARCHAR(50) NOT NULL UNIQUE,
    -- NOT NULL  = @Column(nullable = false)
    -- UNIQUE    = @Column(unique = true)

    description VARCHAR(255),
    -- Không có NOT NULL → có thể null
    -- Tương đương không có nullable = false

    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
    -- Flyway tạo cột, Hibernate/Spring Auditing set giá trị
    );

-- =============================================
-- BẢNG PERMISSIONS
-- =============================================
CREATE TABLE IF NOT EXISTS permissions (
                                           id          BIGSERIAL PRIMARY KEY,
                                           name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(255),
    created_at  TIMESTAMP,
    updated_at  TIMESTAMP
    );

-- =============================================
-- BẢNG ROLE_PERMISSIONS (bảng trung gian)
-- =============================================
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id       BIGINT NOT NULL,
                                                permission_id BIGINT NOT NULL,

                                                PRIMARY KEY (role_id, permission_id),
    -- Composite primary key
    -- Đảm bảo không có cặp (role, permission) trùng nhau

    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE,
    -- ON DELETE CASCADE: xóa role → tự xóa role_permissions
    -- Tránh dữ liệu mồ côi trong bảng trung gian

    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id)
    REFERENCES permissions(id)
    ON DELETE CASCADE
    );

-- =============================================
-- BẢNG USERS
-- =============================================
CREATE TABLE IF NOT EXISTS users (
                                     id         BIGSERIAL PRIMARY KEY,
                                     username   VARCHAR(50)  NOT NULL UNIQUE,
    email      VARCHAR(100) NOT NULL UNIQUE,
    password   VARCHAR(255) NOT NULL,
    -- VARCHAR(255) đủ chứa BCrypt hash
    avatar     VARCHAR(500),
    -- URL ảnh có thể dài
    fullname   VARCHAR(100),
    enabled    BOOLEAN NOT NULL DEFAULT TRUE,
    -- DEFAULT TRUE = @Builder.Default enabled = true
    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- =============================================
-- BẢNG USER_ROLES (bảng trung gian)
-- =============================================
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id BIGINT NOT NULL,
                                          role_id BIGINT NOT NULL,

                                          PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,
    -- Xóa user → tự xóa user_roles

    CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE
    );

-- =============================================
-- BẢNG REFRESH_TOKENS
-- =============================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id             BIGSERIAL PRIMARY KEY,
                                              token          VARCHAR(255) NOT NULL UNIQUE,
    -- UUID 32 ký tự, unique vì mỗi token chỉ có 1 bản ghi
    user_id        BIGINT NOT NULL,
    device_info    VARCHAR(255) NOT NULL,
    expires_at     TIMESTAMP NOT NULL,
    revoked        BOOLEAN NOT NULL DEFAULT FALSE,
    revoked_at     TIMESTAMP,
    -- NULL khi chưa bị thu hồi
    revoked_reason VARCHAR(100),
    created_at     TIMESTAMP,
    updated_at     TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    -- Xóa user → xóa luôn tất cả refresh token của user
    );