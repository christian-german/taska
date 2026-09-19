-- A recurring definition without a schedule has no anchor from which to expand occurrences.
-- Preserve such rows as one-off tasks because no reliable schedule can be inferred.
UPDATE tasks
SET is_recurring   = FALSE,
    recurrence_rule = NULL,
    rrule_ends_at   = NULL
WHERE is_recurring = TRUE
  AND scheduled_at IS NULL;

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_recurring_requires_schedule
        CHECK (is_recurring IS NOT TRUE OR scheduled_at IS NOT NULL);
