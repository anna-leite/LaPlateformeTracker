package com.example.dao;

import com.example.model.User;
import java.util.List;
import java.util.Optional;

/**
 * Interface définissant les opérations CRUD pour les utilisateurs
 * Cette interface suit le pattern Repository pour l'abstraction des données
 */
public interface UserRepository {
    
    /**
     * Sauvegarde un nouvel utilisateur en base de données
     * @param user L'utilisateur à sauvegarder
     * @return L'utilisateur avec son ID généré
     */
    User save(User user);
    
    /**
     * Met à jour un utilisateur existant
     * @param user L'utilisateur à mettre à jour
     * @return L'utilisateur mis à jour
     */
    User update(User user);
    
    /**
     * Trouve un utilisateur par son ID
     * @param id L'ID de l'utilisateur
     * @return Optional contenant l'utilisateur si trouvé
     */
    Optional<User> findById(Integer id);
    
    /**
     * Trouve un utilisateur par son nom d'utilisateur
     * @param username Le nom d'utilisateur
     * @return Optional contenant l'utilisateur si trouvé
     */
    Optional<User> findByUsername(String username);
    
    /**
     * Trouve un utilisateur par son email
     * @param email L'email de l'utilisateur
     * @return Optional contenant l'utilisateur si trouvé
     */
    Optional<User> findByEmail(String email);
    
    /**
     * Récupère tous les utilisateurs
     * @return Liste de tous les utilisateurs
     */
    List<User> findAll();
    
    /**
     * Récupère les utilisateurs avec pagination
     * @param offset Décalage (nombre d'éléments à ignorer)
     * @param limit Nombre maximum d'éléments à retourner
     * @return Liste paginée des utilisateurs
     */
    List<User> findAll(int offset, int limit);
    
    /**
     * Recherche des utilisateurs par nom (prénom ou nom de famille)
     * @param name Le nom à rechercher (partiel autorisé)
     * @return Liste des utilisateurs correspondants
     */
    List<User> findByName(String name);
    
    /**
     * Récupère les utilisateurs par rôle
     * @param role Le rôle à filtrer
     * @return Liste des utilisateurs ayant ce rôle
     */
    List<User> findByRole(User.Role role);
    
    /**
     * Compte le nombre total d'utilisateurs
     * @return Nombre total d'utilisateurs
     */
    long count();
    
    /**
     * Supprime un utilisateur par son ID
     * @param id L'ID de l'utilisateur à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteById(Integer id);
    
    /**
     * Vérifie si un utilisateur existe par son ID
     * @param id L'ID à vérifier
     * @return true si l'utilisateur existe, false sinon
     */
    boolean existsById(Integer id);
    
    /**
     * Vérifie si un nom d'utilisateur est déjà utilisé
     * @param username Le nom d'utilisateur à vérifier
     * @param excludeId ID de l'utilisateur à exclure de la vérification (pour les updates)
     * @return true si le nom d'utilisateur existe déjà, false sinon
     */
    boolean existsByUsername(String username, Integer excludeId);
    
    /**
     * Vérifie si un email est déjà utilisé
     * @param email L'email à vérifier
     * @param excludeId ID de l'utilisateur à exclure de la vérification (pour les updates)
     * @return true si l'email existe déjà, false sinon
     */
    boolean existsByEmail(String email, Integer excludeId);
}
