package com.example.dao;

import com.example.model.User;
import com.example.util.DbConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Implémentation PostgreSQL du repository des utilisateurs
 * Utilise JDBC avec des prepared statements pour la sécurité
 */
public class PostgresUserRepository implements UserRepository {
    private static final Logger logger = LoggerFactory.getLogger(PostgresUserRepository.class);
    private final DbConnectionManager dbManager;

    public PostgresUserRepository(DbConnectionManager dbManager) {
        this.dbManager = dbManager;
    }

    @Override
    public User save(User user) {
        String sql = """
            INSERT INTO app_user (username, password_hash, email, first_name, last_name, role)
            VALUES (?, ?, ?, ?, ?, ?)
            RETURNING id, created_at, updated_at
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getRole().name());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setId(rs.getInt("id"));
                    user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    
                    logger.info("Utilisateur sauvegardé avec l'ID: {}", user.getId());
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la sauvegarde de l'utilisateur: {}", user.getUsername(), e);
            throw new RuntimeException("Erreur de sauvegarde", e);
        }
        
        throw new RuntimeException("Impossible de sauvegarder l'utilisateur");
    }

    @Override
    public User update(User user) {
        String sql = """
            UPDATE app_user 
            SET username = ?, password_hash = ?, email = ?, first_name = ?, last_name = ?, role = ?
            WHERE id = ?
            RETURNING updated_at
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, user.getUsername());
            stmt.setString(2, user.getPasswordHash());
            stmt.setString(3, user.getEmail());
            stmt.setString(4, user.getFirstName());
            stmt.setString(5, user.getLastName());
            stmt.setString(6, user.getRole().name());
            stmt.setInt(7, user.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                    logger.info("Utilisateur mis à jour: {}", user.getUsername());
                    return user;
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur ID: {}", user.getId(), e);
            throw new RuntimeException("Erreur de mise à jour", e);
        }
        
        throw new RuntimeException("Utilisateur non trouvé pour la mise à jour");
    }

    @Override
    public Optional<User> findById(Integer id) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            WHERE id = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'utilisateur ID: {}", id, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            WHERE username = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'utilisateur: {}", username, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
        
        return Optional.empty();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            WHERE email = ?
            """;

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapUserFromResultSet(rs));
                }
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la recherche de l'utilisateur par email: {}", email, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
        
        return Optional.empty();
    }

    @Override
    public List<User> findAll() {
        return findAll(0, Integer.MAX_VALUE);
    }

    @Override
    public List<User> findAll(int offset, int limit) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            ORDER BY last_name, first_name
            LIMIT ? OFFSET ?
            """;

        return executeUserQuery(sql, limit, offset);
    }

    @Override
    public List<User> findByName(String name) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            WHERE LOWER(first_name) LIKE LOWER(?) OR LOWER(last_name) LIKE LOWER(?)
            ORDER BY last_name, first_name
            """;

        String searchPattern = "%" + name + "%";
        return executeUserQuery(sql, searchPattern, searchPattern);
    }

    @Override
    public List<User> findByRole(User.Role role) {
        String sql = """
            SELECT id, username, password_hash, email, first_name, last_name, role, created_at, updated_at
            FROM app_user
            WHERE role = ?
            ORDER BY last_name, first_name
            """;

        return executeUserQuery(sql, role.name());
    }

    @Override
    public long count() {
        String sql = "SELECT COUNT(*) FROM app_user";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            logger.error("Erreur lors du comptage des utilisateurs", e);
            throw new RuntimeException("Erreur de comptage", e);
        }
        
        return 0;
    }

    @Override
    public boolean deleteById(Integer id) {
        String sql = "DELETE FROM app_user WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            int affectedRows = stmt.executeUpdate();
            
            boolean deleted = affectedRows > 0;
            if (deleted) {
                logger.info("Utilisateur supprimé avec l'ID: {}", id);
            }
            
            return deleted;
        } catch (SQLException e) {
            logger.error("Erreur lors de la suppression de l'utilisateur ID: {}", id, e);
            throw new RuntimeException("Erreur de suppression", e);
        }
    }

    @Override
    public boolean existsById(Integer id) {
        String sql = "SELECT 1 FROM app_user WHERE id = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, id);
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification d'existence de l'utilisateur ID: {}", id, e);
            return false;
        }
    }

    @Override
    public boolean existsByUsername(String username, Integer excludeId) {
        String sql = excludeId != null 
            ? "SELECT 1 FROM app_user WHERE username = ? AND id != ?"
            : "SELECT 1 FROM app_user WHERE username = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, username);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification d'username: {}", username, e);
            return false;
        }
    }

    @Override
    public boolean existsByEmail(String email, Integer excludeId) {
        String sql = excludeId != null 
            ? "SELECT 1 FROM app_user WHERE email = ? AND id != ?"
            : "SELECT 1 FROM app_user WHERE email = ?";

        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, email);
            if (excludeId != null) {
                stmt.setInt(2, excludeId);
            }
            
            try (ResultSet rs = stmt.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de la vérification d'email: {}", email, e);
            return false;
        }
    }

    // Méthodes utilitaires privées

    private List<User> executeUserQuery(String sql, Object... params) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }

            try (ResultSet rs = stmt.executeQuery()) {
                List<User> users = new ArrayList<>();
                while (rs.next()) {
                    users.add(mapUserFromResultSet(rs));
                }
                return users;
            }
        } catch (SQLException e) {
            logger.error("Erreur lors de l'exécution de la requête: {}", sql, e);
            throw new RuntimeException("Erreur de requête", e);
        }
    }

    private User mapUserFromResultSet(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPasswordHash(rs.getString("password_hash"));
        user.setEmail(rs.getString("email"));
        user.setFirstName(rs.getString("first_name"));
        user.setLastName(rs.getString("last_name"));
        user.setRole(User.Role.valueOf(rs.getString("role")));
        user.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
        user.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
        return user;
    }
}
