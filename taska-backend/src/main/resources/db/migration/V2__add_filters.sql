CREATE TABLE IF NOT EXISTS filters (
    id         UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    name       TEXT        NOT NULL,
    color      TEXT        NOT NULL DEFAULT 'charcoal',
    position   INTEGER     NOT NULL DEFAULT 0,
    is_favorite BOOLEAN    NOT NULL DEFAULT FALSE,
    project_id UUID        REFERENCES projects(id) ON DELETE SET NULL,
    has_date   BOOLEAN
);

CREATE INDEX IF NOT EXISTS idx_filters_position ON filters(position);
