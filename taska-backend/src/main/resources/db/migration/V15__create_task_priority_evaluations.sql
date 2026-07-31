CREATE TABLE task_priority_evaluation (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    task_id         UUID NOT NULL UNIQUE REFERENCES tasks(id) ON DELETE CASCADE,
    score           INTEGER NOT NULL CHECK (score BETWEEN 0 AND 100),
    components_json JSONB NOT NULL,
    computed_at     TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_task_priority_evaluation_task_id ON task_priority_evaluation(task_id);
