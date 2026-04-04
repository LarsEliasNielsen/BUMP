CREATE TABLE tenants
(
    id            UUID        NOT NULL PRIMARY KEY,
    name          TEXT        NOT NULL,
    status        TEXT        NOT NULL CONSTRAINT tenants_status_check CHECK (status IN ('ACTIVE', 'SUSPENDED')),
    created_at    TIMESTAMPTZ NOT NULL,
    updated_at    TIMESTAMPTZ NOT NULL,
    created_by_id UUID,
    updated_by_id UUID,
    deleted_at    TIMESTAMPTZ,
    deleted_by_id UUID
);
