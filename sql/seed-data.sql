-- Données de test pour le développement

-- Insertion d'un utilisateur administrateur par défaut
-- Mot de passe : admin123 (hasché avec BCrypt)
INSERT INTO app_user (username, password_hash, email, first_name, last_name, role) VALUES 
('admin', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J5pSBVFWL3UjR7qJYHKvCqQ8lFE2zC', 'admin@laplateforme.io', 'Admin', 'System', 'ADMIN'),
('user1', '$2a$10$N9qo8uLOickgx2ZMRZoMye1J5pSBVFWL3UjR7qJYHKvCqQ8lFE2zC', 'user1@laplateforme.io', 'Jean', 'Dupont', 'USER');

-- Insertion d'étudiants de test
INSERT INTO student (first_name, last_name, age, grade) VALUES 
('Pierre', 'Martin', 20, 'A'),
('Marie', 'Dubois', 19, 'B+'),
('Paul', 'Durand', 21, 'C'),
('Sophie', 'Moreau', 20, 'A-'),
('Lucas', 'Bernard', 22, 'B'),
('Emma', 'Petit', 19, 'A+'),
('Thomas', 'Robert', 20, 'C+'),
('Chloé', 'Richard', 21, 'B-'),
('Antoine', 'Leroy', 19, 'C-'),
('Camille', 'Roux', 20, 'A');
