DELETE FROM comments WHERE task_id IS NULL;

DROP INDEX IF EXISTS idx_comments_project;

ALTER TABLE comments
    DROP COLUMN project_id,
    ALTER COLUMN task_id SET NOT NULL;
