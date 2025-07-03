package com.example.service;

import com.example.dao.UserRepository;
import com.example.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.List;
import java.util.Optional;

/**
 * Service de gestion des utilisateurs
 * Contient la logique métier pour les opérations CRUD sur les utilisateurs
 */
public class UserService {
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);
    
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder();
    }
    
    /**
     * Ajoute un nouvel utilisateur
     * @param user L'utilisateur à ajouter
     * @param plainPassword Le mot de passe en clair
     * @return L'utilisateur sauvegardé
     * @throws IllegalArgumentException si les données sont invalides
     */
    public User addUser(User user, String plainPassword) {
        if (user == null) {
            throw new IllegalArgumentException("L'utilisateur ne peut pas être null");
        }
        
        validateUser(user);
        validatePassword(plainPassword);
        
        // Vérifier l'unicité du nom d'utilisateur
        if (userRepository.existsByUsername(user.getUsername(), null)) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
        }
        
        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(user.getEmail(), null)) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        
        try {
            // Hasher le mot de passe
            user.setPasswordHash(passwordEncoder.encode(plainPassword));
            
            User savedUser = userRepository.save(user);
            logger.info("Nouvel utilisateur ajouté: {}", savedUser.getUsername());
            
            return savedUser;
        } catch (Exception e) {
            logger.error("Erreur lors de l'ajout de l'utilisateur: {}", user.getUsername(), e);
            throw new RuntimeException("Impossible d'ajouter l'utilisateur", e);
        }
    }
    
    /**
     * Met à jour un utilisateur existant
     * @param user L'utilisateur à mettre à jour
     * @return L'utilisateur mis à jour
     * @throws IllegalArgumentException si les données sont invalides
     */
    public User updateUser(User user) {
        if (user == null || user.getId() == null) {
            throw new IllegalArgumentException("L'utilisateur et son ID ne peuvent pas être null");
        }
        
        validateUser(user);
        
        // Vérifier que l'utilisateur existe
        if (!userRepository.existsById(user.getId())) {
            throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + user.getId());
        }
        
        // Vérifier l'unicité du nom d'utilisateur
        if (userRepository.existsByUsername(user.getUsername(), user.getId())) {
            throw new IllegalArgumentException("Ce nom d'utilisateur est déjà utilisé");
        }
        
        // Vérifier l'unicité de l'email
        if (userRepository.existsByEmail(user.getEmail(), user.getId())) {
            throw new IllegalArgumentException("Cet email est déjà utilisé");
        }
        
        try {
            User updatedUser = userRepository.update(user);
            logger.info("Utilisateur mis à jour: {}", updatedUser.getUsername());
            
            return updatedUser;
        } catch (Exception e) {
            logger.error("Erreur lors de la mise à jour de l'utilisateur ID: {}", user.getId(), e);
            throw new RuntimeException("Impossible de mettre à jour l'utilisateur", e);
        }
    }
    
    /**
     * Change le mot de passe d'un utilisateur
     * @param userId L'ID de l'utilisateur
     * @param newPassword Le nouveau mot de passe en clair
     * @return true si le changement réussit, false sinon
     */
    public boolean changeUserPassword(Integer userId, String newPassword) {
        if (userId == null) {
            throw new IllegalArgumentException("L'ID utilisateur ne peut pas être null");
        }
        
        validatePassword(newPassword);
        
        try {
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                throw new IllegalArgumentException("Utilisateur non trouvé avec l'ID: " + userId);
            }
            
            User user = userOpt.get();
            user.setPasswordHash(passwordEncoder.encode(newPassword));
            
            userRepository.update(user);
            logger.info("Mot de passe changé pour l'utilisateur: {}", user.getUsername());
            
            return true;
        } catch (Exception e) {
            logger.error("Erreur lors du changement de mot de passe pour l'utilisateur ID: {}", userId, e);
            throw new RuntimeException("Impossible de changer le mot de passe", e);
        }
    }
    
    /**
     * Supprime un utilisateur par son ID
     * @param id L'ID de l'utilisateur à supprimer
     * @return true si la suppression réussit, false sinon
     */
    public boolean deleteUser(Integer id) {
        if (id == null) {
            throw new IllegalArgumentException("L'ID ne peut pas être null");
        }
        
        // Récupérer les informations de l'utilisateur avant suppression
        Optional<User> userOpt = userRepository.findById(id);
        if (userOpt.isEmpty()) {
            logger.warn("Tentative de suppression d'un utilisateur inexistant: {}", id);
            return false;
        }
        
        User user = userOpt.get();
        
        try {
            boolean deleted = userRepository.deleteById(id);
            if (deleted) {
                logger.info("Utilisateur supprimé: {}", user.getUsername());
            }
            return deleted;
        } catch (Exception e) {
            logger.error("Erreur lors de la suppression de l'utilisateur ID: {}", id, e);
            throw new RuntimeException("Impossible de supprimer l'utilisateur", e);
        }
    }
    
    /**
     * Recherche un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findById(Integer id) {
        if (id == null) {
            return null;
        }
        
        try {
            return userRepository.findById(id).orElse(null);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur ID: {}", id, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
    }
    
    /**
     * Recherche un utilisateur par son nom d'utilisateur
     * @param username Le nom d'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        
        try {
            return userRepository.findByUsername(username.trim()).orElse(null);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur: {}", username, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
    }
    
    /**
     * Recherche un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return L'utilisateur trouvé ou null
     */
    public User findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }
        
        try {
            return userRepository.findByEmail(email.trim()).orElse(null);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche de l'utilisateur par email: {}", email, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
    }
    
    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    public List<User> getAllUsers() {
        try {
            return userRepository.findAll();
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération de tous les utilisateurs", e);
            throw new RuntimeException("Erreur de récupération", e);
        }
    }
    
    /**
     * Récupère les utilisateurs avec pagination
     * @param page Numéro de page (commence à 0)
     * @param size Taille de la page
     * @return Liste paginée des utilisateurs
     */
    public List<User> getUsers(int page, int size) {
        if (page < 0 || size <= 0) {
            throw new IllegalArgumentException("Page et taille doivent être positifs");
        }
        
        try {
            int offset = page * size;
            return userRepository.findAll(offset, size);
        } catch (Exception e) {
            logger.error("Erreur lors de la récupération paginée des utilisateurs", e);
            throw new RuntimeException("Erreur de récupération", e);
        }
    }
    
    /**
     * Recherche des utilisateurs par nom
     * @param name Le nom à rechercher (partiel autorisé)
     * @return Liste des utilisateurs correspondants
     */
    public List<User> searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return getAllUsers();
        }
        
        try {
            return userRepository.findByName(name.trim());
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par nom: {}", name, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
    }
    
    /**
     * Récupère les utilisateurs par rôle
     * @param role Le rôle à filtrer
     * @return Liste des utilisateurs ayant ce rôle
     */
    public List<User> getUsersByRole(User.Role role) {
        if (role == null) {
            return getAllUsers();
        }
        
        try {
            return userRepository.findByRole(role);
        } catch (Exception e) {
            logger.error("Erreur lors de la recherche par rôle: {}", role, e);
            throw new RuntimeException("Erreur de recherche", e);
        }
    }
    
    /**
     * Compte le nombre total d'utilisateurs
     * @return Nombre total d'utilisateurs
     */
    public long getTotalCount() {
        try {
            return userRepository.count();
        } catch (Exception e) {
            logger.error("Erreur lors du comptage des utilisateurs", e);
            throw new RuntimeException("Erreur de comptage", e);
        }
    }
    
    /**
     * Vérifie si un nom d'utilisateur est disponible
     * @param username Le nom d'utilisateur à vérifier
     * @param excludeId ID de l'utilisateur à exclure de la vérification (pour les updates)
     * @return true si le nom d'utilisateur est disponible, false sinon
     */
    public boolean isUsernameAvailable(String username, Integer excludeId) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        
        try {
            return !userRepository.existsByUsername(username.trim(), excludeId);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de disponibilité du nom d'utilisateur: {}", username, e);
            return false;
        }
    }
    
    /**
     * Vérifie si un email est disponible
     * @param email L'email à vérifier
     * @param excludeId ID de l'utilisateur à exclure de la vérification (pour les updates)
     * @return true si l'email est disponible, false sinon
     */
    public boolean isEmailAvailable(String email, Integer excludeId) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            return !userRepository.existsByEmail(email.trim(), excludeId);
        } catch (Exception e) {
            logger.error("Erreur lors de la vérification de disponibilité de l'email: {}", email, e);
            return false;
        }
    }
    
    // Méthodes utilitaires privées
    
    private void validateUser(User user) {
        if (!user.isValid()) {
            throw new IllegalArgumentException("Données d'utilisateur invalides");
        }
        
        if (user.getUsername().length() > 50) {
            throw new IllegalArgumentException("Le nom d'utilisateur ne peut pas dépasser 50 caractères");
        }
        
        if (user.getFirstName().length() > 50) {
            throw new IllegalArgumentException("Le prénom ne peut pas dépasser 50 caractères");
        }
        
        if (user.getLastName().length() > 50) {
            throw new IllegalArgumentException("Le nom ne peut pas dépasser 50 caractères");
        }
        
        if (!user.isValidEmail()) {
            throw new IllegalArgumentException("Format d'email invalide");
        }
    }
    
    private void validatePassword(String password) {
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas être vide");
        }
        
        if (password.length() < 6) {
            throw new IllegalArgumentException("Le mot de passe doit contenir au moins 6 caractères");
        }
        
        if (password.length() > 100) {
            throw new IllegalArgumentException("Le mot de passe ne peut pas dépasser 100 caractères");
        }
    }
}
