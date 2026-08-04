CREATE TABLE planning_calendars (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE TABLE planning_calendar_rules (
    id UUID PRIMARY KEY,
    calendar_id UUID NOT NULL REFERENCES planning_calendars(id) ON DELETE CASCADE,
    day_of_week SMALLINT NOT NULL CHECK (day_of_week BETWEEN 1 AND 7),
    start_minute SMALLINT NOT NULL CHECK (start_minute BETWEEN 0 AND 1439),
    end_minute SMALLINT NOT NULL CHECK (end_minute BETWEEN 1 AND 1440),
    CHECK (start_minute < end_minute)
);

INSERT INTO planning_calendars (id, name) VALUES ('00000000-0000-0000-0000-000000000001', 'Default Calendar');
INSERT INTO planning_calendar_rules (id, calendar_id, day_of_week, start_minute, end_minute)
SELECT gen_random_uuid(), '00000000-0000-0000-0000-000000000001', d, 0, 1440 FROM generate_series(1, 7) d;

ALTER TABLE projects ADD COLUMN planning_calendar_id UUID;
UPDATE projects SET planning_calendar_id = '00000000-0000-0000-0000-000000000001';
ALTER TABLE projects ALTER COLUMN planning_calendar_id SET NOT NULL;
ALTER TABLE projects ADD CONSTRAINT fk_projects_planning_calendar FOREIGN KEY (planning_calendar_id) REFERENCES planning_calendars(id);
