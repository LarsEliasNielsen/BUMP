CREATE TABLE users
(
    id            UUID        NOT NULL PRIMARY KEY,
    tenant_id     UUID        NOT NULL REFERENCES tenants (id),
    email         TEXT        NOT NULL UNIQUE,
    password_hash TEXT        NOT NULL,
    role          TEXT        NOT NULL CONSTRAINT users_role_check CHECK (role IN ('DEVELOPER', 'MANAGER', 'ADMIN', 'PLATFORM_ADMIN')),
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    created_by_id UUID,
    updated_by_id UUID,
    deleted_at    TIMESTAMPTZ,
    deleted_by_id UUID
);
