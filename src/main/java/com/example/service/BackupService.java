package com.example.service;

import com.example.util.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Service de gestion des sauvegardes automatiques et manuelles
 * Gère la sauvegarde de la base de données et la programmation des sauvegardes automatiques
 */
public class BackupService {
    private static final Logger logger = LoggerFactory.getLogger(BackupService.class);
    
    private final EventBus eventBus;
    private final ScheduledExecutorService scheduler;
    private final String backupDirectory;
    private final DateTimeFormatter dateFormatter;
    
    // Configuration de la base de données
    private final String dbUrl;
    private final String dbUsername;
    private final String dbPassword;
    
    // Configuration par défaut
    private static final String DEFAULT_BACKUP_DIR = "./backups";
    private static final long DEFAULT_BACKUP_INTERVAL = 300000; // 5 minutes en millisecondes
    private static final int MAX_BACKUP_FILES = 10; // Nombre maximum de fichiers de sauvegarde à conserver
    
    public BackupService(EventBus eventBus, String dbUrl, String dbUsername, String dbPassword) {
        this.eventBus = eventBus;
        this.dbUrl = dbUrl;
        this.dbUsername = dbUsername;
        this.dbPassword = dbPassword;
        this.scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BackupService-Thread");
            t.setDaemon(true);
            return t;
        });
        this.dateFormatter = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        
        // Créer le répertoire de sauvegarde
        this.backupDirectory = initializeBackupDirectory();
        
        // Démarrer les sauvegardes automatiques
        startAutomaticBackup();
        
        logger.info("Service de sauvegarde initialisé. Répertoire: {}", backupDirectory);
    }
    
    /**
     * Effectue une sauvegarde manuelle immédiate
     * @return Le chemin du fichier de sauvegarde créé
     */
    public String performManualBackup() {
        return performBackup("manual");
    }
    
    /**
     * Effectue une sauvegarde automatique
     * @return Le chemin du fichier de sauvegarde créé ou null en cas d'erreur
     */
    public String performAutomaticBackup() {
        return performBackup("auto");
    }
    
    /**
     * Effectue une sauvegarde de la base de données
     * @param type Type de sauvegarde (manual, auto)
     * @return Le chemin du fichier de sauvegarde créé
     */
    private String performBackup(String type) {
        String timestamp = LocalDateTime.now().format(dateFormatter);
        String filename = String.format("laplateforme_tracker_%s_%s.sql", type, timestamp);
        String backupPath = Paths.get(backupDirectory, filename).toString();
        
        try {
            logger.info("Début de la sauvegarde {} vers: {}", type, backupPath);
            
            // Utiliser les informations de connexion du constructeur
            String dbUser = dbUsername;
            
            // Extraire le nom de la base de données de l'URL
            String dbName = extractDatabaseName(dbUrl);
            
            // Construire la commande pg_dump
            ProcessBuilder processBuilder = new ProcessBuilder();
            
            // Configuration pour Windows
            if (System.getProperty("os.name").toLowerCase().contains("windows")) {
                processBuilder.command("cmd", "/c", 
                    String.format("pg_dump -h localhost -U %s -d %s --no-password > \"%s\"", 
                        dbUser, dbName, backupPath));
            } else {
                // Configuration pour Unix/Linux
                processBuilder.command("bash", "-c", 
                    String.format("pg_dump -h localhost -U %s -d %s --no-password > \"%s\"", 
                        dbUser, dbName, backupPath));
            }
            
            // Définir le mot de passe via une variable d'environnement
            processBuilder.environment().put("PGPASSWORD", dbPassword);
            
            // Exécuter la commande
            Process process = processBuilder.start();
            int exitCode = process.waitFor();
            
            if (exitCode == 0) {
                // Vérifier que le fichier a été créé et n'est pas vide
                File backupFile = new File(backupPath);
                if (backupFile.exists() && backupFile.length() > 0) {
                    logger.info("Sauvegarde {} réussie: {} (taille: {} octets)", 
                        type, backupPath, backupFile.length());
                    
                    // Nettoyer les anciennes sauvegardes
                    cleanupOldBackups();
                    
                    // Publier l'événement de sauvegarde réussie
                    eventBus.publish(new EventBus.BackupCompletedEvent(
                        backupPath, true, "Sauvegarde réussie"));
                    
                    return backupPath;
                } else {
                    throw new RuntimeException("Le fichier de sauvegarde est vide ou n'a pas été créé");
                }
            } else {
                throw new RuntimeException("Échec de pg_dump avec le code de sortie: " + exitCode);
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de la sauvegarde {}: {}", type, e.getMessage(), e);
            
            // Publier l'événement d'échec de sauvegarde
            eventBus.publish(new EventBus.BackupCompletedEvent(
                backupPath, false, "Erreur: " + e.getMessage()));
            
            // Supprimer le fichier partiellement créé
            try {
                Files.deleteIfExists(Paths.get(backupPath));
            } catch (IOException deleteEx) {
                logger.warn("Impossible de supprimer le fichier de sauvegarde partiel: {}", backupPath);
            }
            
            return null;
        }
    }
    
    /**
     * Liste tous les fichiers de sauvegarde disponibles
     * @return Liste des fichiers de sauvegarde triés par date (plus récent en premier)
     */
    public List<BackupInfo> listBackups() {
        List<BackupInfo> backups = new ArrayList<>();
        
        try {
            File backupDir = new File(backupDirectory);
            if (!backupDir.exists() || !backupDir.isDirectory()) {
                return backups;
            }
            
            File[] files = backupDir.listFiles((dir, name) -> 
                name.startsWith("laplateforme_tracker_") && name.endsWith(".sql"));
            
            if (files != null) {
                for (File file : files) {
                    try {
                        BackupInfo info = new BackupInfo(
                            file.getName(),
                            file.getAbsolutePath(),
                            file.length(),
                            LocalDateTime.ofInstant(
                                java.time.Instant.ofEpochMilli(file.lastModified()),
                                java.time.ZoneId.systemDefault()
                            )
                        );
                        backups.add(info);
                    } catch (Exception e) {
                        logger.warn("Erreur lors de l'analyse du fichier de sauvegarde: {}", file.getName(), e);
                    }
                }
            }
            
            // Trier par date de création (plus récent en premier)
            backups.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
            
        } catch (Exception e) {
            logger.error("Erreur lors de la liste des sauvegardes", e);
        }
        
        return backups;
    }
    
    /**
     * Supprime un fichier de sauvegarde
     * @param backupPath Le chemin du fichier à supprimer
     * @return true si la suppression réussit, false sinon
     */
    public boolean deleteBackup(String backupPath) {
        if (backupPath == null || backupPath.trim().isEmpty()) {
            return false;
        }
        
        try {
            Path path = Paths.get(backupPath);
            
            // Vérifier que le fichier est dans le répertoire de sauvegarde
            if (!path.startsWith(Paths.get(backupDirectory))) {
                logger.warn("Tentative de suppression d'un fichier hors du répertoire de sauvegarde: {}", backupPath);
                return false;
            }
            
            boolean deleted = Files.deleteIfExists(path);
            if (deleted) {
                logger.info("Fichier de sauvegarde supprimé: {}", backupPath);
            }
            
            return deleted;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de la sauvegarde: {}", backupPath, e);
            return false;
        }
    }
    
    /**
     * Configure l'intervalle des sauvegardes automatiques
     * @param intervalMinutes Intervalle en minutes entre les sauvegardes
     */
    public void setBackupInterval(long intervalMinutes) {
        if (intervalMinutes < 1) {
            throw new IllegalArgumentException("L'intervalle doit être d'au moins 1 minute");
        }
        
        // Redémarrer le planificateur avec le nouvel intervalle
        startAutomaticBackup(intervalMinutes);
        logger.info("Intervalle de sauvegarde automatique mis à jour: {} minutes", intervalMinutes);
    }
    
    /**
     * Arrête le service de sauvegarde
     */
    public void shutdown() {
        logger.info("Arrêt du service de sauvegarde...");
        scheduler.shutdown();
        try {
            if (!scheduler.awaitTermination(10, TimeUnit.SECONDS)) {
                scheduler.shutdownNow();
            }
        } catch (InterruptedException e) {
            scheduler.shutdownNow();
            Thread.currentThread().interrupt();
        }
        logger.info("Service de sauvegarde arrêté");
    }
    
    // Méthodes privées
    
    private String initializeBackupDirectory() {
        String configuredDir = DEFAULT_BACKUP_DIR; // Use default since we don't have properties anymore
        
        try {
            Path path = Paths.get(configuredDir);
            Files.createDirectories(path);
            
            // Vérifier les permissions d'écriture
            if (!Files.isWritable(path)) {
                logger.warn("Répertoire de sauvegarde non accessible en écriture: {}", configuredDir);
                return createFallbackDirectory();
            }
            
            return path.toAbsolutePath().toString();
        } catch (Exception e) {
            logger.error("Impossible de créer le répertoire de sauvegarde: {}", configuredDir, e);
            return createFallbackDirectory();
        }
    }
    
    private String createFallbackDirectory() {
        try {
            Path fallbackPath = Paths.get(System.getProperty("user.home"), "laplateforme_backups");
            Files.createDirectories(fallbackPath);
            logger.info("Utilisation du répertoire de sauvegarde de secours: {}", fallbackPath);
            return fallbackPath.toAbsolutePath().toString();
        } catch (Exception e) {
            logger.error("Impossible de créer le répertoire de sauvegarde de secours", e);
            throw new RuntimeException("Aucun répertoire de sauvegarde disponible", e);
        }
    }
    
    private void startAutomaticBackup() {
        long intervalMs = DEFAULT_BACKUP_INTERVAL; // Use default since we don't have properties anymore
        long intervalMinutes = intervalMs / 60000;
        startAutomaticBackup(intervalMinutes);
    }
    
    private void startAutomaticBackup(long intervalMinutes) {
        // Annuler les tâches précédentes
        scheduler.shutdownNow();
        
        // Redémarrer le scheduler
        ScheduledExecutorService newScheduler = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "BackupService-Thread");
            t.setDaemon(true);
            return t;
        });
        
        // Programmer les sauvegardes automatiques
        newScheduler.scheduleAtFixedRate(() -> {
            try {
                performAutomaticBackup();
            } catch (Exception e) {
                logger.error("Erreur lors de la sauvegarde automatique", e);
            }
        }, intervalMinutes, intervalMinutes, TimeUnit.MINUTES);
        
        logger.info("Sauvegardes automatiques programmées toutes les {} minutes", intervalMinutes);
    }
    
    private void cleanupOldBackups() {
        try {
            List<BackupInfo> backups = listBackups();
            
            if (backups.size() > MAX_BACKUP_FILES) {
                // Supprimer les plus anciens
                for (int i = MAX_BACKUP_FILES; i < backups.size(); i++) {
                    deleteBackup(backups.get(i).getPath());
                }
                logger.info("Nettoyage effectué: {} anciens fichiers supprimés", 
                    backups.size() - MAX_BACKUP_FILES);
            }
        } catch (Exception e) {
            logger.error("Erreur lors du nettoyage des anciennes sauvegardes", e);
        }
    }
    
    private String extractDatabaseName(String jdbcUrl) {
        // Extraire le nom de la base de données depuis l'URL JDBC
        // Format: jdbc:postgresql://host:port/database_name
        if (jdbcUrl == null) {
            return "laplateforme_tracker";
        }
        
        String[] parts = jdbcUrl.split("/");
        if (parts.length > 0) {
            String dbNamePart = parts[parts.length - 1];
            // Supprimer les paramètres éventuels
            int paramIndex = dbNamePart.indexOf('?');
            if (paramIndex > 0) {
                dbNamePart = dbNamePart.substring(0, paramIndex);
            }
            return dbNamePart;
        }
        
        return "laplateforme_tracker";
    }
    
    /**
     * Classe représentant les informations d'une sauvegarde
     */
    public static class BackupInfo {
        private final String name;
        private final String path;
        private final long size;
        private final LocalDateTime createdAt;
        
        public BackupInfo(String name, String path, long size, LocalDateTime createdAt) {
            this.name = name;
            this.path = path;
            this.size = size;
            this.createdAt = createdAt;
        }
        
        public String getName() { return name; }
        public String getPath() { return path; }
        public long getSize() { return size; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        
        public String getFormattedSize() {
            if (size < 1024) return size + " B";
            if (size < 1024 * 1024) return String.format("%.1f KB", size / 1024.0);
            if (size < 1024 * 1024 * 1024) return String.format("%.1f MB", size / (1024.0 * 1024.0));
            return String.format("%.1f GB", size / (1024.0 * 1024.0 * 1024.0));
        }
    }
}
