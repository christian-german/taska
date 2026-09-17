CREATE TABLE task_occurrence_notifications (
    id UUID PRIMARY KEY,
    task_id UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    occurrence_scheduled_at TIMESTAMP WITH TIME ZONE NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    UNIQUE (task_id, occurrence_scheduled_at)
);

CREATE INDEX idx_task_occurrence_notifications_task_id
    ON task_occurrence_notifications(task_id);
