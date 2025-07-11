/* Sample data for development / testing purposes */

-- connect to the right database
\connect laplat_tracker_db;


-- insert sample users (replace hashes with real bcrypt/PBKDF2))
INSERT INTO users (username, email, password_hash, first_name, last_name, role)
VALUES
    ('admin', 'admin@example.com', '$2a$10$XXXXXXXXXXXXXXXXXXXXXXXXXXXXXX', 'Adele', 'Petit', 'admin'),
    ('jdoe', 'jdoe@example.com', '$2a$10$YYYYYYYYYYYYYYYYYYYYYYYYYYYYYY', 'John', 'Doe', 'user');

-- insert sampe students

INSERT INTO students (first_name, last_name, specialization, age, grade)
VALUES
    ('Alice', 'Durand', 'Computer Science', 20, 'A'),
    ('Bob', 'Martin', 'Mathematics', 22, 'B'),
    ('Charlie', 'Dupont', 'Physics', 21, 'C'),
    ('Diana', 'Lefevre', 'Chemistry', 23, 'B'),
    ('Eve', 'Moreau', 'Biology', 19, 'A+'),
    ('Frank', 'Bernard', 'Engineering', 24, 'B+'),
    ('Grace', 'Petit', 'Economics', 20, 'A-'),
    ('Hank', 'Garnier', 'History', 22, 'C+'),
    ('Ivy', 'Rousseau', 'Literature', 21, 'B-'),
    ('Jack', 'Lemoine', 'Art', 23, 'A');