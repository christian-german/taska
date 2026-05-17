-- V9 applied AT TIME ZONE 'Europe/Paris' to all tasks, which shifted all-day tasks by
-- the Paris UTC offset (e.g. 2h in summer). All-day task times are meaningless
-- (always 00:00:00) and should remain midnight UTC, not midnight Paris time.
--
-- Fix: for each all-day task, take the UTC value stored by V9, read its Paris display,
-- then reinterpret that display as UTC → restores midnight UTC.
-- Example: 2026-05-16 22:00:00Z → (Paris) 2026-05-17 00:00:00 → (UTC) 2026-05-17 00:00:00Z

UPDATE tasks
SET due_at = (due_at AT TIME ZONE 'Europe/Paris') AT TIME ZONE 'UTC'
WHERE all_day = true
  AND due_at IS NOT NULL;
