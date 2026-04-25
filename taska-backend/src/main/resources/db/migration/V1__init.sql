CREATE TABLE IF NOT EXISTS projects (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(255) NOT NULL,
    color       VARCHAR(50)  NOT NULL DEFAULT 'charcoal',
    parent_id   UUID REFERENCES projects(id) ON DELETE CASCADE,
    position    INTEGER      NOT NULL DEFAULT 0,
    is_favorite BOOLEAN      NOT NULL DEFAULT FALSE,
    view_style  VARCHAR(10)  NOT NULL DEFAULT 'LIST',
    is_inbox_project BOOLEAN NOT NULL DEFAULT FALSE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS sections (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name       VARCHAR(255) NOT NULL,
    project_id UUID         NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
    position   INTEGER      NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE IF NOT EXISTS tasks (
    id            UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    content       VARCHAR(1000) NOT NULL,
    description   TEXT,
    project_id    UUID REFERENCES projects(id) ON DELETE CASCADE,
    section_id    UUID REFERENCES sections(id) ON DELETE SET NULL,
    parent_id     UUID REFERENCES tasks(id) ON DELETE CASCADE,
    position      INTEGER      NOT NULL DEFAULT 0,
    priority      INTEGER      NOT NULL DEFAULT 1,
    is_completed  BOOLEAN      NOT NULL DEFAULT FALSE,
    due_date      DATE,
    due_date_time TIMESTAMP,
    is_recurring  BOOLEAN      NOT NULL DEFAULT FALSE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT now(),
    completed_at  TIMESTAMPTZ
);

CREATE TABLE IF NOT EXISTS task_labels (
    task_id UUID        NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    label   VARCHAR(100) NOT NULL,
    PRIMARY KEY (task_id, label)
);

CREATE TABLE IF NOT EXISTS labels (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL UNIQUE,
    color       VARCHAR(50)  NOT NULL DEFAULT 'charcoal',
    position    INTEGER      NOT NULL DEFAULT 0,
    is_favorite BOOLEAN      NOT NULL DEFAULT FALSE
);

CREATE TABLE IF NOT EXISTS comments (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id    UUID REFERENCES tasks(id) ON DELETE CASCADE,
    project_id UUID REFERENCES projects(id) ON DELETE CASCADE,
    content    TEXT        NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE INDEX IF NOT EXISTS idx_tasks_project_id  ON tasks(project_id);
CREATE INDEX IF NOT EXISTS idx_tasks_section_id  ON tasks(section_id);
CREATE INDEX IF NOT EXISTS idx_tasks_parent_id   ON tasks(parent_id);
CREATE INDEX IF NOT EXISTS idx_tasks_due_date    ON tasks(due_date);
CREATE INDEX IF NOT EXISTS idx_tasks_completed   ON tasks(is_completed);
CREATE INDEX IF NOT EXISTS idx_sections_project  ON sections(project_id);
CREATE INDEX IF NOT EXISTS idx_comments_task     ON comments(task_id);
CREATE INDEX IF NOT EXISTS idx_comments_project  ON comments(project_id);

INSERT INTO projects (id, name, color, is_inbox_project) VALUES (gen_random_uuid(), 'Inbox', 'charcoal', TRUE);
