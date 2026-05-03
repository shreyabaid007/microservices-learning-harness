CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE tasks (
    id           UUID                     PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id      UUID                     NOT NULL,
    title        VARCHAR(255)             NOT NULL,
    description  TEXT,
    due_date     DATE,
    is_completed BOOLEAN                  NOT NULL DEFAULT FALSE,
    created_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updated_at   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);

CREATE INDEX idx_tasks_user_id ON tasks(user_id);
