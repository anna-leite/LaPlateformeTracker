package com.example.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Gestionnaire de connexions à la base de données utilisant HikariCP pour le pooling
 * Version simplifiée pour être utilisée avec AppContext
 */
public class DbConnectionManager {
    private static final Logger logger = LoggerFactory.getLogger(DbConnectionManager.class);
    private HikariDataSource dataSource;
    private final String url;
    private final String username;
    private final String password;

    /**
     * Constructeur avec paramètres de connexion
     */
    public DbConnectionManager(String url, String username, String password) {
        this.url = url;
        this.username = username;
        this.password = password;
        initializeDataSource();
    }

    /**
     * Initialise le pool de connexions HikariCP
     */
    private void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // Configuration de base
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName("org.postgresql.Driver");
            
            // Configuration du pool
            config.setMaximumPoolSize(10);
            config.setMinimumIdle(2);
            config.setConnectionTimeout(30000);
            config.setIdleTimeout(600000);
            config.setMaxLifetime(1800000);
            
            // Configuration additionnelle pour PostgreSQL
            config.addDataSourceProperty("cachePrepStmts", "true");
            config.addDataSourceProperty("prepStmtCacheSize", "250");
            config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
            config.addDataSourceProperty("useServerPrepStmts", "true");
            
            // Test de connexion
            config.setConnectionTestQuery("SELECT 1");
            
            // Nom du pool pour le monitoring
            config.setPoolName("LaPlateformeTracker-DB-Pool");
            
            dataSource = new HikariDataSource(config);
            
            // Test de la connexion initiale
            try (Connection testConnection = dataSource.getConnection()) {
                logger.info("Pool de connexions initialisé avec succès. URL: {}", url);
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du pool de connexions", e);
            throw new RuntimeException("Impossible d'initialiser la base de données", e);
        }
    }

    /**
     * Obtient une connexion depuis le pool
     * @return Connection active à la base de données
     * @throws SQLException si impossible d'obtenir une connexion
     */
    public Connection getConnection() throws SQLException {
        if (dataSource == null || dataSource.isClosed()) {
            logger.error("Le pool de connexions n'est pas initialisé ou est fermé");
            throw new SQLException("Pool de connexions non disponible");
        }
        
        Connection connection = dataSource.getConnection();
        logger.debug("Connexion obtenue depuis le pool. Connexions actives: {}", 
            dataSource.getHikariPoolMXBean().getActiveConnections());
        
        return connection;
    }

    /**
     * Teste la connectivité à la base de données
     * @return true si la connexion fonctionne, false sinon
     */
    public boolean testConnection() {
        try (Connection connection = getConnection()) {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            logger.warn("Test de connexion échoué", e);
            return false;
        }
    }

    /**
     * Obtient les statistiques du pool de connexions
     */
    public String getPoolStats() {
        if (dataSource == null || dataSource.isClosed()) {
            return "Pool de connexions non initialisé";
        }
        
        var poolMXBean = dataSource.getHikariPoolMXBean();
        return String.format(
            "Pool Stats - Active: %d, Idle: %d, Total: %d, Waiting: %d",
            poolMXBean.getActiveConnections(),
            poolMXBean.getIdleConnections(),
            poolMXBean.getTotalConnections(),
            poolMXBean.getThreadsAwaitingConnection()
        );
    }

    /**
     * Ferme le pool de connexions
     * Cette méthode doit être appelée lors de l'arrêt de l'application
     */
    public void close() {
        if (dataSource != null && !dataSource.isClosed()) {
            logger.info("Fermeture du pool de connexions...");
            dataSource.close();
            logger.info("Pool de connexions fermé");
        }
    }
}
