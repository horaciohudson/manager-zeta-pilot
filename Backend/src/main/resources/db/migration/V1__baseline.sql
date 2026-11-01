CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE tab_roles (
    role_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name VARCHAR(50) NOT NULL UNIQUE,
    CONSTRAINT ck_tab_roles_name CHECK (name IN ('ROLE_ADMIN', 'ROLE_USER'))
);

CREATE TABLE tab_users (
    user_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    full_name VARCHAR(150),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    email_verified BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE tab_users_roles (
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_tab_users_roles_user FOREIGN KEY (user_id)
        REFERENCES tab_users(user_id) ON DELETE CASCADE,
    CONSTRAINT fk_tab_users_roles_role FOREIGN KEY (role_id)
        REFERENCES tab_roles(role_id) ON DELETE CASCADE
);

CREATE TABLE tab_refresh_tokens (
    refresh_token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tab_refresh_tokens_user FOREIGN KEY (user_id)
        REFERENCES tab_users(user_id) ON DELETE CASCADE
);

CREATE TABLE tab_email_verification_tokens (
    email_verification_token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tab_email_verification_tokens_user FOREIGN KEY (user_id)
        REFERENCES tab_users(user_id) ON DELETE CASCADE
);

CREATE TABLE tab_password_reset_tokens (
    password_reset_token_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL,
    token_hash VARCHAR(255) NOT NULL UNIQUE,
    expires_at TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    used_at TIMESTAMP WITHOUT TIME ZONE,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tab_password_reset_tokens_user FOREIGN KEY (user_id)
        REFERENCES tab_users(user_id) ON DELETE CASCADE
);

CREATE TABLE tab_audit_log (
    audit_log_id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    actor_user_id UUID,
    action VARCHAR(100) NOT NULL,
    entity_type VARCHAR(100) NOT NULL,
    entity_id VARCHAR(100),
    description TEXT,
    ip_address VARCHAR(64),
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_tab_audit_log_actor FOREIGN KEY (actor_user_id)
        REFERENCES tab_users(user_id) ON DELETE SET NULL
);

CREATE INDEX idx_tab_users_active ON tab_users(active);
CREATE INDEX idx_tab_refresh_tokens_user ON tab_refresh_tokens(user_id);
CREATE INDEX idx_tab_refresh_tokens_expiry ON tab_refresh_tokens(expires_at);
CREATE INDEX idx_tab_email_verification_tokens_user ON tab_email_verification_tokens(user_id);
CREATE INDEX idx_tab_email_verification_tokens_expiry ON tab_email_verification_tokens(expires_at);
CREATE INDEX idx_tab_password_reset_tokens_user ON tab_password_reset_tokens(user_id);
CREATE INDEX idx_tab_password_reset_tokens_expiry ON tab_password_reset_tokens(expires_at);
CREATE INDEX idx_tab_audit_log_actor ON tab_audit_log(actor_user_id);
CREATE INDEX idx_tab_audit_log_created_at ON tab_audit_log(created_at);

INSERT INTO tab_roles (name)
VALUES ('ROLE_ADMIN'), ('ROLE_USER');