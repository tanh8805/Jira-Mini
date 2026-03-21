CREATE TABLE users (
                       id UUID PRIMARY KEY,
                       email VARCHAR(255) NOT NULL UNIQUE,
                       password_hash VARCHAR(255) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_email ON users(email);

CREATE TABLE refresh_tokens (
                                id UUID PRIMARY KEY,
                                user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                token VARCHAR(512) NOT NULL UNIQUE,
                                revoked BOOLEAN NOT NULL DEFAULT FALSE,
                                expires_at TIMESTAMPTZ NOT NULL,
                                created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_refresh_tokens_token ON refresh_tokens(token);
CREATE INDEX idx_refresh_tokens_user_id ON refresh_tokens(user_id);

CREATE TABLE projects (
                          id UUID PRIMARY KEY,
                          name VARCHAR(100) NOT NULL,
                          description TEXT,
                          owner_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                          created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                          updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_projects_owner_id ON projects(owner_id);

CREATE TABLE project_members (
                                 id UUID PRIMARY KEY,
                                 project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                                 user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
                                 role VARCHAR(20) NOT NULL DEFAULT 'MEMBER',
                                 joined_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                 CONSTRAINT uq_project_members UNIQUE (project_id, user_id)
);

CREATE INDEX idx_project_members_project_id ON project_members(project_id);
CREATE INDEX idx_project_members_user_id ON project_members(user_id);

CREATE TABLE tasks (
                       id UUID PRIMARY KEY,
                       project_id UUID NOT NULL REFERENCES projects(id) ON DELETE CASCADE,
                       assignee_id UUID REFERENCES users(id) ON DELETE SET NULL,
                       title VARCHAR(200) NOT NULL,
                       description TEXT,
                       status VARCHAR(20) NOT NULL DEFAULT 'TODO',
                       priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
                       due_date DATE,
                       created_by UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
                       created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                       updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_project_id ON tasks(project_id);
CREATE INDEX idx_tasks_assignee_id ON tasks(assignee_id);
CREATE INDEX idx_tasks_status ON tasks(status);

CREATE TABLE audit_logs (
                            id UUID PRIMARY KEY,
                            actor_id UUID REFERENCES users(id) ON DELETE SET NULL,
                            entity_type VARCHAR(50) NOT NULL,
                            entity_id UUID NOT NULL,
                            action VARCHAR(20) NOT NULL,
                            old_value JSONB,
                            new_value JSONB,
                            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_logs_entity ON audit_logs(entity_type, entity_id);
CREATE INDEX idx_audit_logs_actor_id ON audit_logs(actor_id);
CREATE INDEX idx_audit_logs_created_at ON audit_logs(created_at DESC);

INSERT INTO users (id, email, password_hash, full_name, role)
VALUES
    ('a0000000-0000-0000-0000-000000000001','admin@taskflow.dev','$2a$10$7EqJtq98hPqEX7fNZaFWoOe3prNmL/X2FdXbJBhbHwGBJMT8ZXkAC','System Admin','ADMIN'),
    ('a0000000-0000-0000-0000-000000000002','dev@taskflow.dev','$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LnESE2YdowG','Dev User','USER');

INSERT INTO projects (id, name, description, owner_id)
VALUES
    ('b0000000-0000-0000-0000-000000000001','TaskFlow Backend','Main backend project','a0000000-0000-0000-0000-000000000001');

INSERT INTO project_members (id, project_id, user_id, role)
VALUES
    ('c0000000-0000-0000-0000-000000000001','b0000000-0000-0000-0000-000000000001','a0000000-0000-0000-0000-000000000001','OWNER'),
    ('c0000000-0000-0000-0000-000000000002','b0000000-0000-0000-0000-000000000001','a0000000-0000-0000-0000-000000000002','MEMBER');

INSERT INTO tasks (id, project_id, assignee_id, title, status, priority, created_by)
VALUES
    ('d0000000-0000-0000-0000-000000000001','b0000000-0000-0000-0000-000000000001','a0000000-0000-0000-0000-000000000002','Setup Spring Boot project','DONE','HIGH','a0000000-0000-0000-0000-000000000001'),
    ('d0000000-0000-0000-0000-000000000002','b0000000-0000-0000-0000-000000000001','a0000000-0000-0000-0000-000000000002','Implement JWT auth','IN_PROGRESS','HIGH','a0000000-0000-0000-0000-000000000001'),
    ('d0000000-0000-0000-0000-000000000003','b0000000-0000-0000-0000-000000000001',NULL,'Write API documentation','TODO','MEDIUM','a0000000-0000-0000-0000-000000000001');