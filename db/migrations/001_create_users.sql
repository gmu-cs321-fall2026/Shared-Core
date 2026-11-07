-- 001_create_users.sql
-- authored by Yiting He and Ethan Hayes

-- Account table: login info only. Owned by Shared Core.
-- password_hash must NEVER be returned by any API endpoint.

BEGIN TRANSACTION;

CREATE EXTENSION IF NOT EXISTS pgcrypto;  --for gen_random_uuid(), apparently it's included in newer versions but keeping this here for now.

/*
Enums are case-sensitive and are going to return a clear error to the client if there isn't a match,
so no need for additional constraints there. 
NOTE: our API should validate that the enum values are correct beforehand, for simplicity of error-handling!
*/
CREATE TYPE user_status AS ENUM ('active', 'suspended', 'deleted');
CREATE TYPE access_level AS ENUM ('student', 'mentor', 'career', 'admin');

CREATE TABLE users (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email           VARCHAR(320) UNIQUE NOT NULL,
    password_hash   VARCHAR(255) NOT NULL,    --NOTE: hashing will be done by the API, and only hashed passwords will be queried.
    first_name      VARCHAR(255) NOT NULL,
    last_name       VARCHAR(255) NOT NULL,
    role            access_level NOT NULL DEFAULT 'student',
    status          user_status  NOT NULL DEFAULT 'active',
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT now(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT now()
);

-- before we make indexes, let's actually show that our API expects them to be common queries.

/*
Triggers are sort of obnoxious, but generally how they work is they're passed a NEW struct,
as well as an OLD struct if it's an UPDATE (but NEW is the guaranteed). In this example, when
a user is updating or inserting a user, I grab the NEW struct from the query BEFORE the data is stored,
modify its email field, and return the struct I want to be stored (which is NEW, since I did an in-place modification).

I define the function first and then I create the trigger using the function afterwards.
*/
CREATE FUNCTION return_lowercase_email() RETURNS TRIGGER AS -- only stores an email in all-lowercase.
$$ BEGIN
    NEW.email := lower(NEW.email);
    RETURN NEW;
END $$
LANGUAGE plpgsql;

CREATE TRIGGER users_lower_email
    BEFORE INSERT OR UPDATE ON users  
    FOR EACH ROW EXECUTE FUNCTION return_lowercase_email(); --this means each row that's modified, not each row in the whole table.

CREATE FUNCTION update_timestamp() RETURNS TRIGGER AS --updates the updated_at value automatically.
$$ BEGIN
    NEW.updated_at := now();
    RETURN NEW;
END $$
LANGUAGE plpgsql;

CREATE TRIGGER users_updated_at
    BEFORE UPDATE ON users
    FOR EACH ROW EXECUTE FUNCTION update_timestamp(); 
    
--[ THIS SECTION MARKS A NEW TABLE; MIGRATE TO SEPARATE FILE EVENTUALLY PLEASE! ]

CREATE TABLE mentor_profile (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associated_user UUID UNIQUE NOT NULL REFERENCES users(id) --UNIQUE so that a user can't have multiple mentor profiles
);

--[ THIS SECTION MARKS A NEW TABLE; MIGRATE TO SEPARATE FILE EVENTUALLY PLEASE! ]

CREATE TABLE student_profile (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    associated_user UUID UNIQUE NOT NULL REFERENCES users(id) --UNIQUE so that a user can't have multiple student profiles
);

COMMIT;