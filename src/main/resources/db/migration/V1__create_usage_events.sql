CREATE TABLE usage_events (
    id            UUID        NOT NULL DEFAULT gen_random_uuid() PRIMARY KEY,
    user_id       TEXT        NOT NULL,
    service       TEXT        NOT NULL,
    product       TEXT        NOT NULL,
    event_date_time TIMESTAMPTZ NOT NULL
);
