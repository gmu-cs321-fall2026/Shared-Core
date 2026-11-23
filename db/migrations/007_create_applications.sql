-- create_applications.sql
-- authored by Trinity Himes

-- applications table

CREATE TYPE application_status AS ENUM {
  'submitted',
  'under_review',
  'rejected',
  'accepted',
  'withdrawn'
};

CREATE TABLE applications {
  application_id  INT PRIMARY KEY,
  job_id          INT,
  user_id         UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
  status          application_status NOT NULL DEFAULT 'submitted',
  resume_url      TEXT,
  updated_at      TIMESTAMPTZ NOT NULL DEFAULT now(),
  submitted_at    TIMESTAMPTZ NOT NULL DEFAULT now(),
  
  FOREIGN KEY (job_id) REFERENCES jobs(job_id)
};

CREATE INDEX applications_job_id_idx ON applications(job_id);
CREATE INDEX applications_status_idx ON applications(status)

-- create the functions if needed
