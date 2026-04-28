ALTER TABLE tasks ADD COLUMN IF NOT EXISTS estimate_minutes INTEGER;
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS mention_context VARCHAR(100);
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS recurrence_rule VARCHAR(100);

CREATE INDEX IF NOT EXISTS idx_tasks_completed_at ON tasks(completed_at);
