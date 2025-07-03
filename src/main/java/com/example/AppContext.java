package com.example;

import com.example.dao.DAOFactory;
import com.example.dao.StudentRepository;
import com.example.service.StudentService;
import com.example.util.DbConnectionManager;
import com.example.util.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Contexte de l'application qui centralise l'initialisation et la gestion des singletons
 * Version simplifiée pour notre modèle Student
 */
public class AppContext {
    private static final Logger logger = LoggerFactory.getLogger(AppContext.class);
    private static AppContext instance;
    
    // Configuration
    private Properties applicationProperties;
    
    // Gestionnaires de base
    private DbConnectionManager dbConnectionManager;
    private EventBus eventBus;
    private DAOFactory daoFactory;
    
    // Services
    private StudentService studentService;
    
    private boolean initialized = false;
    
    private AppContext() {
        // Constructeur privé pour le pattern Singleton
    }
    
    /**
     * Obtient l'instance unique du contexte de l'application
     */
    public static synchronized AppContext getInstance() {
        if (instance == null) {
            instance = new AppContext();
        }
        return instance;
    }
    
    /**
     * Initialise le contexte de l'application
     */
    public synchronized void initialize() {
        if (initialized) {
            logger.warn("Le contexte de l'application est déjà initialisé");
            return;
        }
        
        try {
            logger.info("Initialisation du contexte de l'application...");
            
            // 1. Charger la configuration
            loadConfiguration();
            
            // 2. Initialiser les gestionnaires de base
            initializeManagers();
            
            // 3. Initialiser les services
            initializeServices();
            
            initialized = true;
            logger.info("Contexte de l'application initialisé avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation du contexte de l'application", e);
            throw new RuntimeException("Impossible d'initialiser l'application", e);
        }
    }
    
    /**
     * Arrête proprement l'application
     */
    public synchronized void shutdown() {
        if (!initialized) {
            return;
        }
        
        logger.info("Arrêt de l'application...");
        
        try {
            // Fermer la connexion à la base de données
            if (dbConnectionManager != null) {
                dbConnectionManager.close();
            }
            
            initialized = false;
            logger.info("Application arrêtée proprement");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt de l'application", e);
        }
    }
    
    // Getters pour les services
    
    public StudentService getStudentService() {
        checkInitialized();
        return studentService;
    }
    
    /**
     * Obtient le service d'authentification
     * Note: Non implémenté dans la version simplifiée
     */
    public Object getAuthService() {
        // TODO: Implémenter l'authentification si nécessaire
        return null;
    }
    
    public EventBus getEventBus() {
        checkInitialized();
        return eventBus;
    }
    
    public DbConnectionManager getDbConnectionManager() {
        checkInitialized();
        return dbConnectionManager;
    }
    
    public Properties getApplicationProperties() {
        checkInitialized();
        return applicationProperties;
    }
    
    // Méthodes privées d'initialisation
    
    private void loadConfiguration() {
        logger.info("Chargement de la configuration...");
        
        applicationProperties = new Properties();
        
        // Charger les propriétés depuis le fichier de configuration
        try (InputStream input = getClass().getClassLoader().getResourceAsStream("application.properties")) {
            if (input != null) {
                applicationProperties.load(input);
                logger.info("Configuration chargée depuis application.properties");
            } else {
                logger.warn("Fichier application.properties non trouvé, utilisation des valeurs par défaut");
                loadDefaultProperties();
            }
        } catch (IOException e) {
            logger.warn("Erreur lors du chargement de application.properties, utilisation des valeurs par défaut", e);
            loadDefaultProperties();
        }
    }
    
    private void loadDefaultProperties() {
        // Propriétés par défaut
        applicationProperties.setProperty("database.url", "jdbc:postgresql://localhost:5432/laplateforme_tracker");
        applicationProperties.setProperty("database.username", "postgres");
        applicationProperties.setProperty("database.password", "password");
        applicationProperties.setProperty("database.driver", "org.postgresql.Driver");
        applicationProperties.setProperty("database.pool.maxSize", "10");
    }
    
    private void initializeManagers() {
        logger.info("Initialisation des gestionnaires...");
        
        // EventBus
        eventBus = EventBus.getInstance();
        
        // Gestionnaire de base de données
        dbConnectionManager = new DbConnectionManager(
            applicationProperties.getProperty("database.url"),
            applicationProperties.getProperty("database.username"),
            applicationProperties.getProperty("database.password")
        );
        
        // Factory DAO
        daoFactory = new DAOFactory(dbConnectionManager);
        
        logger.info("Gestionnaires initialisés");
    }
    
    private void initializeServices() {
        logger.info("Initialisation des services...");
        
        // Repository
        StudentRepository studentRepository = daoFactory.getStudentRepository();
        
        // Services
        studentService = new StudentService(studentRepository, eventBus);
        
        logger.info("Services initialisés");
    }
    
    private void checkInitialized() {
        if (!initialized) {
            throw new IllegalStateException("Le contexte de l'application n'est pas initialisé");
        }
    }
    
    /**
     * Indique si l'application est initialisée
     */
    public boolean isInitialized() {
        return initialized;
    }
}
