ALTER TABLE usage_events
    ADD COLUMN idempotency_key TEXT NOT NULL,
    ADD CONSTRAINT usage_events_idempotency_key_unique UNIQUE (idempotency_key);
