ALTER TABLE users
RENAME COLUMN role TO role_id;

ALTER TABLE users
ALTER COLUMN role_id TYPE INT;

ALTER TABLE users
ADD CONSTRAINT fk_roles_users
FOREIGN KEY (role_id) REFERENCES roles(id);