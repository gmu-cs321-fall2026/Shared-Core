-- 001_seed_users.sql
-- Test data for local development ONLY. Never use these passwords anywhere real.
-- All test passwords are: ChangeMe123!
-- crypt(..., gen_salt('bf')) makes a bcrypt hash, which most login libraries can check.

INSERT INTO users (id, email, password_hash, roles) VALUES
  ('00000000-0000-0000-0000-000000000001', 'admin@gmu.edu',    crypt('ChangeMe123!', gen_salt('bf', 10)), '{admin}'),
  ('00000000-0000-0000-0000-000000000002', 'staff@gmu.edu',    crypt('ChangeMe123!', gen_salt('bf', 10)), '{staff}'),
  ('00000000-0000-0000-0000-000000000003', 'jdoe@gmu.edu',     crypt('ChangeMe123!', gen_salt('bf', 10)), '{student}'),
  ('00000000-0000-0000-0000-000000000004', 'grad@gmu.edu',     crypt('ChangeMe123!', gen_salt('bf', 10)), '{student,mentor}'),
  ('00000000-0000-0000-0000-000000000005', 'sam.lee@example.com', crypt('ChangeMe123!', gen_salt('bf', 10)), '{mentor}');

INSERT INTO profiles (user_id, full_name, grad_year, major, employer, job_title, extensions) VALUES
  ('00000000-0000-0000-0000-000000000001', 'Platform Admin',  NULL, NULL, NULL, NULL, '{}'),
  ('00000000-0000-0000-0000-000000000002', 'Career Staff',    NULL, NULL, NULL, NULL, '{}'),
  ('00000000-0000-0000-0000-000000000003', 'Jane Doe',        2027, 'Computer Science', NULL, NULL,
     '{"tracker": {"target_roles": ["software intern"]}}'),
  ('00000000-0000-0000-0000-000000000004', 'Alex Kim',        2026, 'Information Technology', NULL, NULL,
     '{"mentor_matching": {"industries": ["tech"], "guidance_areas": ["mock interviews"]}}'),
  ('00000000-0000-0000-0000-000000000005', 'Sam Lee',         2018, 'Finance', 'Capital One', 'Senior Analyst',
     '{"mentor_matching": {"industries": ["finance"], "guidance_areas": ["resume review"]}}');
