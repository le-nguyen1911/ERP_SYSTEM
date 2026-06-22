-- Auth Module - UUID Version

CREATE EXTENSION IF NOT EXISTS pgcrypto SCHEMA public;
-- =============================================
-- BẢNG ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS roles (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(50) NOT NULL UNIQUE,

    description VARCHAR(255),

    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- =============================================
-- BẢNG PERMISSIONS
-- =============================================
CREATE TABLE IF NOT EXISTS permissions (
                                           id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    name VARCHAR(100) NOT NULL UNIQUE,

    description VARCHAR(255),

    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- =============================================
-- BẢNG ROLE_PERMISSIONS
-- =============================================
CREATE TABLE IF NOT EXISTS role_permissions (
                                                role_id UUID NOT NULL,
                                                permission_id UUID NOT NULL,

                                                PRIMARY KEY (role_id, permission_id),

    CONSTRAINT fk_role_permissions_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_role_permissions_permission
    FOREIGN KEY (permission_id)
    REFERENCES permissions(id)
    ON DELETE CASCADE
    );

-- =============================================
-- BẢNG USERS
-- =============================================
CREATE TABLE IF NOT EXISTS users (
                                     id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    username VARCHAR(50) NOT NULL UNIQUE,

    email VARCHAR(100) NOT NULL UNIQUE,

    password VARCHAR(255) NOT NULL,

    avatar VARCHAR(500),

    fullname VARCHAR(100),

    enabled BOOLEAN NOT NULL DEFAULT TRUE,

    created_at TIMESTAMP,
    updated_at TIMESTAMP
    );

-- =============================================
-- BẢNG USER_ROLES
-- =============================================
CREATE TABLE IF NOT EXISTS user_roles (
                                          user_id UUID NOT NULL,
                                          role_id UUID NOT NULL,

                                          PRIMARY KEY (user_id, role_id),

    CONSTRAINT fk_user_roles_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE,

    CONSTRAINT fk_user_roles_role
    FOREIGN KEY (role_id)
    REFERENCES roles(id)
    ON DELETE CASCADE
    );

-- =============================================
-- BẢNG REFRESH_TOKENS
-- =============================================
CREATE TABLE IF NOT EXISTS refresh_tokens (
                                              id UUID PRIMARY KEY DEFAULT gen_random_uuid(),

    token VARCHAR(255) NOT NULL UNIQUE,

    user_id UUID NOT NULL,

    device_info VARCHAR(255) NOT NULL,

    expires_at TIMESTAMP NOT NULL,

    revoked BOOLEAN NOT NULL DEFAULT FALSE,

    revoked_at TIMESTAMP,

    revoked_reason VARCHAR(100),

    created_at TIMESTAMP,
    updated_at TIMESTAMP,

    CONSTRAINT fk_refresh_tokens_user
    FOREIGN KEY (user_id)
    REFERENCES users(id)
    ON DELETE CASCADE
    );

-- =============================================
-- INDEX
-- =============================================
CREATE INDEX IF NOT EXISTS idx_users_username
    ON users(username);

CREATE INDEX IF NOT EXISTS idx_users_email
    ON users(email);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_user_id
    ON refresh_tokens(user_id);

CREATE INDEX IF NOT EXISTS idx_refresh_tokens_active
    ON refresh_tokens(user_id, revoked)
    WHERE revoked = FALSE;