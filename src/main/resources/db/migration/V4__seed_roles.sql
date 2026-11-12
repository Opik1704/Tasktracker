-- Справочник ролей, необходимый для регистрации пользователей
INSERT INTO roles (name) VALUES
    ('ROLE_EMPLOYEE'),
    ('ROLE_TEAM_LEAD'),
    ('ROLE_DIRECTOR'),
    ('ROLE_ADMIN')
ON CONFLICT (name) DO NOTHING;
