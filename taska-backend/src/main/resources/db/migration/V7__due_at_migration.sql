-- Ajout des nouvelles colonnes
ALTER TABLE tasks ADD COLUMN due_at TIMESTAMP NULL;
ALTER TABLE tasks ADD COLUMN all_day BOOLEAN NOT NULL DEFAULT FALSE;

-- Migration : due_date_time prioritaire
UPDATE tasks
SET due_at  = due_date_time,
    all_day = FALSE
WHERE due_date_time IS NOT NULL;

-- Migration : due_date seul → minuit, all_day = true
UPDATE tasks
SET due_at  = (due_date::TIMESTAMP),
    all_day = TRUE
WHERE due_date_time IS NULL AND due_date IS NOT NULL;

-- Suppression des anciennes colonnes et de leur index
DROP INDEX IF EXISTS idx_tasks_due_date;
ALTER TABLE tasks DROP COLUMN due_date;
ALTER TABLE tasks DROP COLUMN due_date_time;

-- Nouvel index
CREATE INDEX idx_tasks_due_at ON tasks(due_at);
