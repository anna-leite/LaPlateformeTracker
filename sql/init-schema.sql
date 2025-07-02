-- 1. CREATE DATABASE (run as a super-user or existing owner)

DROP DATABASE IF EXISTS laplat_tracker_db;
CREATE DATABASE laplat_tracker_db
  WITH OWNER = postgres
       ENCODING = 'UTF8'
       LC_COLLATE = 'en_US.UTF-8'
       LC_CTYPE = 'en_US.UTF-8'
       TEMPLATE = template0
       CONNECTION LIMIT = -1;
  
\connect laplat_tracker_bd;

-- Create the USER table

DROP TABLE IF EXISTS users CASCADE;
CREATE TABLE users (
  id SERIAL PRIMARY KEY,
  username VARCHAR(50) UNIQUE NOT NULL,
  email VARCHAR(100) UNIQUE NOT NULL,
  password_hash VARCHAR(255) NOT NULL, -- store bcrypt hash
  first_name VARCHAR(100),
  last_name VARCHAR(100),
  is_active  BOOLEAN NOT NULL DEFAULT TRUE,
  role  VARCHAR(20) NOT NULL CHECK (role IN ('admin','user')) DEFAULT 'user',
  last_login TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

COMMENT ON TABLE users IS 'System users who can log in to LaPlateforme Tracker';

-- Create the STUDENT table

DROP TABLE IF EXISTS students CASCADE;
CREATE TABLE students (
  id SERIAL PRIMARY KEY,
  first_name VARCHAR(100) NOT NULL,
  last_name VARCHAR(100) NOT NULL,
  specialization VARCHAR(100) NOT NULL,
  age INTEGER  NOT NULL CHECK (age BETWEEN 0 AND 150),
  grade VARCHAR(3), -- allow grades like "A+", "B-", "10", "100" ect.
  created_at TIMESTAMP NOT NULL DEFAULT now(),
  updated_at TIMESTAMP NOT NULL DEFAULT now()
);

COMMENT ON TABLE students IS 'Student records including personal info, specialization and grade average';

-- (Optional) Grant privileges to the laplat_tracker_user

GRANT SELECT, INSERT, UPDATE, DELETE
  ON TABLE students, users,
  TO laplat_tracker_user;