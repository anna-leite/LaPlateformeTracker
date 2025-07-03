package com.example.model;

import java.time.LocalDateTime;

/**
 * Modèle représentant un utilisateur du système
 * Cette classe encapsule les informations d'authentification et de profil d'un utilisateur
 */
public class User {
    private Integer id;
    private String username;
    private String passwordHash;
    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Énumération pour les rôles
    public enum Role {
        ADMIN("Administrateur"),
        USER("Utilisateur");

        private final String displayName;

        Role(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }
    }

    // Constructeurs
    public User() {
        this.role = Role.USER;
    }

    public User(String username, String passwordHash, String email, String firstName, String lastName) {
        this();
        this.username = username;
        this.passwordHash = passwordHash;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public User(String username, String passwordHash, String email, String firstName, String lastName, Role role) {
        this(username, passwordHash, email, firstName, lastName);
        this.role = role;
    }

    // Getters et Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Méthodes utilitaires
    /**
     * Retourne le nom complet de l'utilisateur
     */
    public String getFullName() {
        return firstName + " " + lastName;
    }

    /**
     * Vérifie si l'utilisateur est administrateur
     */
    public boolean isAdmin() {
        return role == Role.ADMIN;
    }

    /**
     * Validation des données de l'utilisateur
     */
    public boolean isValid() {
        return username != null && !username.trim().isEmpty() &&
               passwordHash != null && !passwordHash.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               firstName != null && !firstName.trim().isEmpty() &&
               lastName != null && !lastName.trim().isEmpty() &&
               role != null;
    }

    /**
     * Validation du format email
     */
    public boolean isValidEmail() {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        return email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }

    @Override
    public String toString() {
        return String.format("User{id=%d, username='%s', name='%s', email='%s', role=%s}",
                id, username, getFullName(), email, role);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        User user = (User) obj;
        return id != null && id.equals(user.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}
