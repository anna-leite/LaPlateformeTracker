package com.example.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Système d'événements permettant la communication découplée entre les composants
 * Implémente le pattern Observer pour permettre aux différentes parties de l'application
 * de réagir aux changements sans dépendances directes
 */
public class EventBus {
    private static final Logger logger = LoggerFactory.getLogger(EventBus.class);
    private static EventBus instance;
    
    // Map des listeners organisés par type d'événement
    private final Map<Class<?>, CopyOnWriteArrayList<Consumer<Object>>> listeners;
    
    private EventBus() {
        this.listeners = new ConcurrentHashMap<>();
    }
    
    /**
     * Obtient l'instance unique de l'EventBus (Singleton)
     */
    public static synchronized EventBus getInstance() {
        if (instance == null) {
            instance = new EventBus();
        }
        return instance;
    }
    
    /**
     * Abonne un listener à un type d'événement spécifique
     * @param eventType Type d'événement à écouter
     * @param listener Fonction à exécuter lors de la réception de l'événement
     */
    @SuppressWarnings("unchecked")
    public <T> void subscribe(Class<T> eventType, Consumer<T> listener) {
        listeners.computeIfAbsent(eventType, k -> new CopyOnWriteArrayList<>())
                 .add((Consumer<Object>) listener);
        logger.debug("Listener ajouté pour le type d'événement: {}", eventType.getSimpleName());
    }
    
    /**
     * Désabonne un listener d'un type d'événement
     * @param eventType Type d'événement
     * @param listener Listener à désabonner
     */
    @SuppressWarnings("unchecked")
    public <T> void unsubscribe(Class<T> eventType, Consumer<T> listener) {
        CopyOnWriteArrayList<Consumer<Object>> eventListeners = listeners.get(eventType);
        if (eventListeners != null) {
            eventListeners.remove((Consumer<Object>) listener);
            logger.debug("Listener supprimé pour le type d'événement: {}", eventType.getSimpleName());
        }
    }
    
    /**
     * Publie un événement à tous les listeners abonnés
     * @param event L'événement à publier
     */
    public void publish(Object event) {
        if (event == null) {
            logger.warn("Tentative de publication d'un événement null");
            return;
        }
        
        Class<?> eventType = event.getClass();
        CopyOnWriteArrayList<Consumer<Object>> eventListeners = listeners.get(eventType);
        
        if (eventListeners != null && !eventListeners.isEmpty()) {
            logger.debug("Publication de l'événement {} à {} listeners", 
                eventType.getSimpleName(), eventListeners.size());
            
            for (Consumer<Object> listener : eventListeners) {
                try {
                    listener.accept(event);
                } catch (Exception e) {
                    logger.error("Erreur lors de l'exécution d'un listener pour l'événement {}", 
                        eventType.getSimpleName(), e);
                }
            }
        } else {
            logger.debug("Aucun listener pour l'événement {}", eventType.getSimpleName());
        }
    }
    
    /**
     * Obtient le nombre de listeners pour un type d'événement
     */
    public int getListenerCount(Class<?> eventType) {
        CopyOnWriteArrayList<Consumer<Object>> eventListeners = listeners.get(eventType);
        return eventListeners != null ? eventListeners.size() : 0;
    }
    
    /**
     * Supprime tous les listeners
     */
    public void clear() {
        listeners.clear();
        logger.info("Tous les listeners ont été supprimés");
    }
    
    // Classes d'événements prédéfinies
    
    /**
     * Événement publié lors de l'ajout d'un étudiant
     */
    public static class StudentAddedEvent {
        private final Integer studentId;
        private final String studentName;
        
        public StudentAddedEvent(Integer studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
        }
        
        public Integer getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
    }
    
    /**
     * Événement publié lors de la modification d'un étudiant
     */
    public static class StudentUpdatedEvent {
        private final Integer studentId;
        private final String studentName;
        
        public StudentUpdatedEvent(Integer studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
        }
        
        public Integer getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
    }
    
    /**
     * Événement publié lors de la suppression d'un étudiant
     */
    public static class StudentDeletedEvent {
        private final Integer studentId;
        private final String studentName;
        
        public StudentDeletedEvent(Integer studentId, String studentName) {
            this.studentId = studentId;
            this.studentName = studentName;
        }
        
        public Integer getStudentId() { return studentId; }
        public String getStudentName() { return studentName; }
    }
    
    /**
     * Événement publié lors d'une connexion utilisateur
     */
    public static class UserLoginEvent {
        private final String username;
        private final String role;
        
        public UserLoginEvent(String username, String role) {
            this.username = username;
            this.role = role;
        }
        
        public String getUsername() { return username; }
        public String getRole() { return role; }
    }
    
    /**
     * Événement publié lors d'une déconnexion utilisateur
     */
    public static class UserLogoutEvent {
        private final String username;
        
        public UserLogoutEvent(String username) {
            this.username = username;
        }
        
        public String getUsername() { return username; }
    }
    
    /**
     * Événement publié lors d'une sauvegarde automatique
     */
    public static class BackupCompletedEvent {
        private final String backupPath;
        private final boolean success;
        private final String message;
        
        public BackupCompletedEvent(String backupPath, boolean success, String message) {
            this.backupPath = backupPath;
            this.success = success;
            this.message = message;
        }
        
        public String getBackupPath() { return backupPath; }
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
    }
}
