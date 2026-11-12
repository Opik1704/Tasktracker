-- Добавить колонку completed_at в таблицу tasks
ALTER TABLE tasks ADD COLUMN IF NOT EXISTS completed_at TIMESTAMP(6);

-- Добавить индекс на completed_at
CREATE INDEX IF NOT EXISTS idx_tasks_completed_at ON tasks (completed_at);
