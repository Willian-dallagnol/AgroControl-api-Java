-- Usuario administrador inicial para testes.
-- Credenciais: admin@agrocontrol.dev / admin123
-- Hash BCrypt (strength 10) gerado fora da aplicacao.
INSERT INTO users (name, email, password_hash, role, active, created_at, updated_at)
VALUES (
    'Administrador',
    'admin@agrocontrol.dev',
    '$2b$10$t/taSEOju6zWZtt9Vb4y6uHhmvD4uTHSBcl9I9KAOEewahKy.8bP.',
    'ADMIN',
    TRUE,
    now(),
    now()
);
