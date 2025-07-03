package com.example.service;

import com.example.dao.UserRepository;
import com.example.model.User;
import com.example.util.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

/**
 * Service d'authentification gérant les connexions et sessions utilisateur
 * Utilise BCrypt pour le hachage sécurisé des mots de passe
 */
public class AuthService {
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final EventBus eventBus;
    
    private User currentUser;
    
    public AuthService(UserRepository userRepository, EventBus eventBus) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
        this.eventBus = eventBus;
    }
    
    /**
     * Authentifie un utilisateur avec nom d'utilisateur et mot de passe
     * @param username Nom d'utilisateur
     * @param password Mot de passe en clair
     * @return true si l'authentification réussit, false sinon
     */
    public boolean login(String username, String password) {
        if (username == null || username.trim().isEmpty() || 
            password == null || password.trim().isEmpty()) {
            logger.warn("Tentative de connexion avec des identifiants vides");
            return false;
        }
        
        try {
            Optional<User> userOpt = userRepository.findByUsername(username.trim());
            
            if (userOpt.isEmpty()) {
                logger.warn("Tentative de connexion avec un nom d'utilisateur inexistant: {}", username);
                return false;
            }
            
            User user = userOpt.get();
            
            if (passwordEncoder.matches(password, user.getPasswordHash())) {
                this.currentUser = user;
                logger.info("Connexion réussie pour l'utilisateur: {}", username);
                
                // Publier l'événement de connexion
                eventBus.publish(new EventBus.UserLoginEvent(username, user.getRole().name()));
                
                return true;
            } else {
                logger.warn("Mot de passe incorrect pour l'utilisateur: {}", username);
                return false;
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'authentification de l'utilisateur: {}", username, e);
            return false;
        }
    }
    
    /**
     * Déconnecte l'utilisateur actuel
     */
    public void logout() {
        if (currentUser != null) {
            String username = currentUser.getUsername();
            logger.info("Déconnexion de l'utilisateur: {}", username);
            
            // Publier l'événement de déconnexion
            eventBus.publish(new EventBus.UserLogoutEvent(username));
            
            currentUser = null;
        }
    }
    
    /**
     * Vérifie si un utilisateur est connecté
     * @return true si un utilisateur est connecté, false sinon
     */
    public boolean isLoggedIn() {
        return currentUser != null;
    }
    
    /**
     * Obtient l'utilisateur actuellement connecté
     * @return L'utilisateur connecté ou null si aucun utilisateur n'est connecté
     */
    public User getCurrentUser() {
        return currentUser;
    }
    
    /**
     * Vérifie si l'utilisateur connecté est administrateur
     * @return true si l'utilisateur est admin, false sinon
     */
    public boolean isCurrentUserAdmin() {
        return currentUser != null && currentUser.isAdmin();
    }
    
    /**
     * Change le mot de passe de l'utilisateur connecté
     * @param currentPassword Mot de passe actuel
     * @param newPassword Nouveau mot de passe
     * @return true si le changement réussit, false sinon
     */
    public boolean changePassword(String currentPassword, String newPassword) {
        if (!isLoggedIn()) {
            logger.warn("Tentative de changement de mot de passe sans utilisateur connecté");
            return false;
        }
        
        if (currentPassword == null || newPassword == null || 
            currentPassword.trim().isEmpty() || newPassword.trim().isEmpty()) {
            logger.warn("Tentative de changement de mot de passe avec des valeurs vides");
            return false;
        }
        
        if (newPassword.length() < 6) {
            logger.warn("Tentative de changement de mot de passe trop court");
            return false;
        }
        
        try {
            // Vérifier le mot de passe actuel
            if (!passwordEncoder.matches(currentPassword, currentUser.getPasswordHash())) {
                logger.warn("Mot de passe actuel incorrect pour l'utilisateur: {}", currentUser.getUsername());
                return false;
            }
            
            // Hasher le nouveau mot de passe
            String newPasswordHash = passwordEncoder.encode(newPassword);
            currentUser.setPasswordHash(newPasswordHash);
            
            // Mettre à jour en base
            userRepository.update(currentUser);
            
            logger.info("Mot de passe changé avec succès pour l'utilisateur: {}", currentUser.getUsername());
            return true;
            
        } catch (Exception e) {
            logger.error("Erreur lors du changement de mot de passe pour l'utilisateur: {}", 
                currentUser.getUsername(), e);
            return false;
        }
    }
    
    /**
     * Hache un mot de passe en utilisant BCrypt
     * @param plainPassword Mot de passe en clair
     * @return Mot de passe haché
     */
    public String hashPassword(String plainPassword) {
        if (plainPassword == null || plainPassword.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        
        return passwordEncoder.encode(plainPassword);
    }
    
    /**
     * Vérifie si un mot de passe correspond à son hash
     * @param plainPassword Mot de passe en clair
     * @param hashedPassword Mot de passe haché
     * @return true si le mot de passe correspond, false sinon
     */
    public boolean verifyPassword(String plainPassword, String hashedPassword) {
        if (plainPassword == null || hashedPassword == null) {
            return false;
        }
        
        try {
            return passwordEncoder.matches(plainPassword, hashedPassword);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification du mot de passe", e);
            return false;
        }
    }
    
    /**
     * Valide la force d'un mot de passe
     * @param password Mot de passe à valider
     * @return Message de validation (vide si le mot de passe est valide)
     */
    public String validatePasswordStrength(String password) {
        if (password == null || password.isEmpty()) {
            return "Le mot de passe ne peut pas être vide";
        }
        
        if (password.length() < 6) {
            return "Le mot de passe doit contenir au moins 6 caractères";
        }
        
        if (password.length() > 100) {
            return "Le mot de passe ne peut pas dépasser 100 caractères";
        }
        
        // Vérifier la présence de différents types de caractères
        boolean hasLetter = password.matches(".*[a-zA-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        
        if (!hasLetter) {
            return "Le mot de passe doit contenir au moins une lettre";
        }
        
        if (!hasDigit) {
            return "Le mot de passe doit contenir au moins un chiffre";
        }
        
        return ""; // Mot de passe valide
    }
}
