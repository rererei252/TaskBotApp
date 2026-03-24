CREATE TABLE IF NOT EXISTS tasks (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL,
    title VARCHAR(100) NOT NULL,
    start_at TIMESTAMPTZ NOT NULL,
    due_at TIMESTAMPTZ NOT NULL,
    priority VARCHAR(10) NOT NULL CHECK (priority IN ('low', 'medium', 'high')),
    status VARCHAR(10) NOT NULL DEFAULT 'todo' CHECK (status IN ('todo', 'doing', 'done')),
    detail TEXT NULL,
    location_url TEXT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    deleted_at TIMESTAMPTZ NULL,
    CONSTRAINT fk_tasks_user
        FOREIGN KEY (user_id)
        REFERENCES users(id)
        ON DELETE CASCADE,
    CONSTRAINT chk_tasks_due_after_start
        CHECK (due_at >= start_at)
);

CREATE INDEX IF NOT EXISTS idx_tasks_user_id
    ON tasks(user_id);

CREATE INDEX IF NOT EXISTS idx_tasks_user_start_at
    ON tasks(user_id, start_at);

CREATE INDEX IF NOT EXISTS idx_tasks_user_due_at
    ON tasks(user_id, due_at);

CREATE INDEX IF NOT EXISTS idx_tasks_user_status
    ON tasks(user_id, status);
