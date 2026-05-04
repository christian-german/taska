CREATE TABLE IF NOT EXISTS time_entries (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    start_at    TIMESTAMP    NOT NULL,
    end_at      TIMESTAMP    NOT NULL,
    project_id  UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    description VARCHAR(500) NOT NULL DEFAULT '',
    notes       TEXT,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_time_entries_project_id ON time_entries(project_id);
CREATE INDEX IF NOT EXISTS idx_time_entries_start_at   ON time_entries(start_at);
