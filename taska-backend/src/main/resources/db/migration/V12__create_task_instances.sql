CREATE TABLE task_instances (
    id             UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    task_id        UUID NOT NULL REFERENCES tasks(id) ON DELETE CASCADE,
    scheduled_at   TIMESTAMP WITH TIME ZONE NOT NULL,
    due_at         TIMESTAMP WITH TIME ZONE,
    status         VARCHAR(20) NOT NULL,
    completed_at   TIMESTAMP WITH TIME ZONE,
    title          VARCHAR(1000),
    priority       INTEGER,
    created_at     TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    updated_at     TIMESTAMP WITH TIME ZONE DEFAULT now() NOT NULL,
    UNIQUE (task_id, scheduled_at)
);

CREATE INDEX idx_task_instances_task_id     ON task_instances(task_id);
CREATE INDEX idx_task_instances_scheduled_at ON task_instances(scheduled_at);
CREATE INDEX idx_task_instances_status       ON task_instances(status);
