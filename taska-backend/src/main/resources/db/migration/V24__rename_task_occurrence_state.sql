ALTER TABLE task_instances
    RENAME TO task_occurrence_states;

ALTER TABLE task_occurrence_states
    RENAME COLUMN task_id TO series_id;

ALTER TABLE task_occurrence_states
    RENAME CONSTRAINT task_instances_pkey TO task_occurrence_states_pkey;

ALTER TABLE task_occurrence_states
    RENAME CONSTRAINT task_instances_task_id_fkey TO task_occurrence_states_series_id_fkey;

ALTER TABLE task_occurrence_states
    RENAME CONSTRAINT task_instances_task_id_scheduled_at_key
        TO task_occurrence_states_series_id_occurrence_scheduled_at_key;

ALTER INDEX idx_task_instances_task_id
    RENAME TO idx_task_occurrence_states_series_id;

ALTER INDEX idx_task_instances_scheduled_at
    RENAME TO idx_task_occurrence_states_occurrence_scheduled_at;

ALTER INDEX idx_task_instances_status
    RENAME TO idx_task_occurrence_states_status;

ALTER TABLE task_occurrence_notifications
    RENAME COLUMN task_id TO series_id;

ALTER TABLE task_occurrence_notifications
    RENAME CONSTRAINT task_occurrence_notifications_task_id_fkey
        TO task_occurrence_notifications_series_id_fkey;

-- PostgreSQL truncates the V23-generated unique-constraint name to 63 characters. Resolve it by
-- table and type rather than relying on that implementation-generated identifier.
DO $$
DECLARE
    notification_unique_constraint NAME;
BEGIN
    SELECT constraint_name
      INTO STRICT notification_unique_constraint
      FROM information_schema.table_constraints
     WHERE table_schema = current_schema()
       AND table_name = 'task_occurrence_notifications'
       AND constraint_type = 'UNIQUE';

    EXECUTE format(
        'ALTER TABLE task_occurrence_notifications RENAME CONSTRAINT %I TO task_occurrence_notifications_series_occurrence_key',
        notification_unique_constraint
    );
END
$$;

ALTER INDEX idx_task_occurrence_notifications_task_id
    RENAME TO idx_task_occurrence_notifications_series_id;
