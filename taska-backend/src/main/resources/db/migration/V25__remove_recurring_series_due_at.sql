UPDATE tasks
SET due_at = NULL
WHERE is_recurring = TRUE;

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_recurring_due_at_null
        CHECK (is_recurring IS NOT TRUE OR due_at IS NULL);
