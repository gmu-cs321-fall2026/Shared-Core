-- 004_create_roles.sql
-- authored by Trinity Himes

 -- The table that makes it RBAC.

CREATE TABLE roles (
  id UUID PRIMARY KEY,
  role_name VARCHAR(16) NOT NULL UNIQUE,
  description TEXT,
  created_at TIMESTAMPZ NOT NULL DEFAULT now(),
  updated_at TIMESTAMPZ NOT NULL DEFAULT now()
);