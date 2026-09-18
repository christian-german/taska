-- A row claiming to recur without a rule is not a series: it generates nothing, is excluded from
-- calendar expansion, and makes the recurrence expander throw wherever it is reached. Such rows
-- are what they effectively are, a scheduled one-off task.
UPDATE tasks
SET is_recurring  = FALSE,
    rrule_ends_at = NULL
WHERE is_recurring = TRUE
  AND (recurrence_rule IS NULL OR btrim(recurrence_rule) = '');

ALTER TABLE tasks
    ADD CONSTRAINT chk_tasks_recurring_requires_rule
        CHECK (is_recurring IS NOT TRUE
            OR (recurrence_rule IS NOT NULL AND btrim(recurrence_rule) <> ''));
