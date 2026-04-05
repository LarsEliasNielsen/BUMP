TRUNCATE TABLE usage_events;
ALTER TABLE usage_events
    DROP COLUMN user_id,
    ADD COLUMN tenant_id UUID NOT NULL REFERENCES tenants(id),
    ADD COLUMN user_id   UUID NOT NULL REFERENCES users(id);
