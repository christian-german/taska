ALTER TABLE tasks
    ALTER COLUMN priority DROP DEFAULT;

ALTER TABLE tasks
    ALTER COLUMN priority DROP NOT NULL;

ALTER TABLE tasks
    RENAME COLUMN due_at TO scheduled_at;

ALTER TABLE task_instances
    RENAME COLUMN scheduled_at TO occurrence_scheduled_at;

ALTER TABLE task_instances
    RENAME COLUMN due_at TO scheduled_at;
