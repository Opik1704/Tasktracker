-- Уникальность имени тега без учёта регистра
CREATE UNIQUE INDEX IF NOT EXISTS idx_tags_name_lower ON tags (LOWER(name));
