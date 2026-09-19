-- An occurrence state is anchored to an instant its series generates. When a rule change stops
-- generating that instant, the state is not deleted: it is detached. A detached DONE keeps a
-- completion the user recorded, a detached MODIFIED keeps something the user deliberately placed
-- on that date. Both stay attached to their series through series_id and remain displayable.
ALTER TABLE task_occurrence_states
    ADD COLUMN detached BOOLEAN NOT NULL DEFAULT FALSE;

-- A skip is an absence: once the occurrence it excluded is no longer generated, it has no object.
DELETE FROM task_occurrence_states s
    USING tasks t
WHERE s.series_id = t.id
  AND s.status = 'SKIPPED'
  AND t.rrule_ends_at IS NOT NULL
  AND s.occurrence_scheduled_at >= t.rrule_ends_at;

-- States already orphaned by an earlier truncation become detached rather than staying invisible.
UPDATE task_occurrence_states s
SET detached = TRUE
FROM tasks t
WHERE s.series_id = t.id
  AND t.rrule_ends_at IS NOT NULL
  AND s.occurrence_scheduled_at >= t.rrule_ends_at;

CREATE INDEX idx_task_occurrence_states_detached
    ON task_occurrence_states (detached, scheduled_at, occurrence_scheduled_at);
