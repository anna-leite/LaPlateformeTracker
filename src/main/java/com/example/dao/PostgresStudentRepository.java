package com.example.dao;

import com.example.model.Student;
import com.example.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation PostgreSQL du repository pour les étudiants
 * Utilise JDBC avec des prepared statements pour la sécurité
 */
public class PostgresStudentRepository implements StudentRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresStudentRepository.class);
    private final DbConnectionManager dbManager;

    public PostgresStudentRepository(DbConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public Student save(Student student) {
        String sql = "INSERT INTO student (first_name, last_name, age, grade) VALUES (?, ?, ?, ?) RETURNING id, created_at, updated_at";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setInt(3, student.getAge());
            stmt.setString(4, student.getGrade());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    student.setId(rs.getInt("id"));
                    student.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    student.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    logger.info("Étudiant sauvegardé avec l'ID: {}", student.getId());
                    return student;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la sauvegarde de l'étudiant", e);
            throw new RuntimeException("Impossible de sauvegarder l'étudiant", e);
        }
        
        throw new RuntimeException("Échec de la sauvegarde de l'étudiant");
    }

    @Override
    public Student update(Student student) {
        String sql = "UPDATE student SET first_name = ?, last_name = ?, age = ?, grade = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ? RETURNING updated_at";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, student.getFirstName());
            stmt.setString(2, student.getLastName());
            stmt.setInt(3, student.getAge());
            stmt.setString(4, student.getGrade());
            stmt.setInt(5, student.getId());
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    student.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    logger.info("Étudiant mis à jour avec l'ID: {}", student.getId());
                    return student;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'étudiant ID: {}", student.getId(), e);
            throw new RuntimeException("Impossible de mettre à jour l'étudiant", e);
        }
        
        throw new RuntimeException("Étudiant non trouvé pour la mise à jour");
    }

    @Override
    public Optional<Student> findById(Integer id) {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToStudent(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'étudiant ID: {}", id, e);
            throw new RuntimeException("Impossible de récupérer l'étudiant", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<Student> findAll() {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student ORDER BY last_name, first_name";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération de tous les étudiants", e);
            throw new RuntimeException("Impossible de récupérer les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findAll(int offset, int limit) {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student ORDER BY last_name, first_name LIMIT ? OFFSET ?";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération paginée des étudiants", e);
            throw new RuntimeException("Impossible de récupérer les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findByName(String name) {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student WHERE LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?) ORDER BY last_name, first_name";
        
        List<Student> students = new ArrayList<>();
        String searchPattern = "%" + name + "%";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, searchPattern);
            stmt.setString(2, searchPattern);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par nom: {}", name, e);
            throw new RuntimeException("Impossible de rechercher les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findByAgeRange(int minAge, int maxAge) {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student WHERE age BETWEEN ? AND ? ORDER BY age, last_name, first_name";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, minAge);
            stmt.setInt(2, maxAge);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par tranche d'âge: {}-{}", minAge, maxAge, e);
            throw new RuntimeException("Impossible de rechercher les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findByGrade(String grade) {
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student WHERE grade = ? ORDER BY last_name, first_name";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, grade);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par note: {}", grade, e);
            throw new RuntimeException("Impossible de rechercher les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findByGradeRange(double minGrade, double maxGrade) {
        // Convertir les notes numériques en lettres pour la recherche
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student WHERE grade IS NOT NULL ORDER BY last_name, first_name";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Student student = mapResultSetToStudent(rs);
                double numericGrade = student.getNumericGrade();
                if (numericGrade >= minGrade && numericGrade <= maxGrade) {
                    students.add(student);
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche par plage de notes: {}-{}", minGrade, maxGrade, e);
            throw new RuntimeException("Impossible de rechercher les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findAllSorted(String sortBy, boolean ascending) {
        String orderDirection = ascending ? "ASC" : "DESC";
        String orderBy;
        
        switch (sortBy.toLowerCase()) {
            case "firstname":
            case "first_name":
                orderBy = "first_name " + orderDirection + ", last_name " + orderDirection;
                break;
            case "lastname":
            case "last_name":
                orderBy = "last_name " + orderDirection + ", first_name " + orderDirection;
                break;
            case "age":
                orderBy = "age " + orderDirection + ", last_name ASC, first_name ASC";
                break;
            case "grade":
                orderBy = "grade " + orderDirection + ", last_name ASC, first_name ASC";
                break;
            default:
                orderBy = "last_name " + orderDirection + ", first_name " + orderDirection;
        }
        
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student ORDER BY " + orderBy;
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                students.add(mapResultSetToStudent(rs));
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération triée des étudiants", e);
            throw new RuntimeException("Impossible de récupérer les étudiants", e);
        }
        
        return students;
    }

    @Override
    public List<Student> findAllSorted(String sortBy, boolean ascending, int offset, int limit) {
        String orderDirection = ascending ? "ASC" : "DESC";
        String orderBy;
        
        switch (sortBy.toLowerCase()) {
            case "firstname":
            case "first_name":
                orderBy = "first_name " + orderDirection + ", last_name " + orderDirection;
                break;
            case "lastname":
            case "last_name":
                orderBy = "last_name " + orderDirection + ", first_name " + orderDirection;
                break;
            case "age":
                orderBy = "age " + orderDirection + ", last_name ASC, first_name ASC";
                break;
            case "grade":
                orderBy = "grade " + orderDirection + ", last_name ASC, first_name ASC";
                break;
            default:
                orderBy = "last_name " + orderDirection + ", first_name " + orderDirection;
        }
        
        String sql = "SELECT id, first_name, last_name, age, grade, created_at, updated_at FROM student ORDER BY " + orderBy + " LIMIT ? OFFSET ?";
        
        List<Student> students = new ArrayList<>();
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, limit);
            stmt.setInt(2, offset);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    students.add(mapResultSetToStudent(rs));
                }
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la récupération triée et paginée des étudiants", e);
            throw new RuntimeException("Impossible de récupérer les étudiants", e);
        }
        
        return students;
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM student";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getLong(1);
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des étudiants", e);
            throw new RuntimeException("Impossible de compter les étudiants", e);
        }
        
        return 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM student WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            int rowsAffected = stmt.executeUpdate();
            
            if (rowsAffected > 0) {
                logger.info("Étudiant supprimé avec l'ID: {}", id);
                return true;
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'étudiant ID: {}", id, e);
            throw new RuntimeException("Impossible de supprimer l'étudiant", e);
        }
        
        return false;
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM student WHERE id = ?";
        
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, id);
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
            
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification d'existence de l'étudiant ID: {}", id, e);
            throw new RuntimeException("Impossible de vérifier l'existence de l'étudiant", e);
        }
    }

    /**
     * Mappe un ResultSet vers un objet Student
     */
    private Student mapResultSetToStudent(ResultSet rs) throws SQLException {
        Student student = new Student();
        student.setId(rs.getInt("id"));
        student.setFirstName(rs.getString("first_name"));
        student.setLastName(rs.getString("last_name"));
        student.setAge(rs.getInt("age"));
        student.setGrade(rs.getString("grade"));
        
        Timestamp createdAt = rs.getTimestamp("created_at");
        if (createdAt != null) {
            student.setCreatedAt(createdAt.toLocalDateTime());
        }
        
        Timestamp updatedAt = rs.getTimestamp("updated_at");
        if (updatedAt != null) {
            student.setUpdatedAt(updatedAt.toLocalDateTime());
        }
        
        return student;
    }
}
