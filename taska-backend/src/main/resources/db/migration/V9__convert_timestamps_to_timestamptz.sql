-- Converts naive TIMESTAMP columns (implicitly Europe/Paris) to TIMESTAMPTZ stored as UTC.
-- Existing values have no timezone info but were always entered/displayed in Europe/Paris.

ALTER TABLE tasks
    ALTER COLUMN due_at TYPE TIMESTAMPTZ
    USING due_at AT TIME ZONE 'Europe/Paris';

ALTER TABLE time_entries
    ALTER COLUMN start_at TYPE TIMESTAMPTZ
    USING start_at AT TIME ZONE 'Europe/Paris';

ALTER TABLE time_entries
    ALTER COLUMN end_at TYPE TIMESTAMPTZ
    USING end_at AT TIME ZONE 'Europe/Paris';
