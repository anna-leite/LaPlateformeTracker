package com.example.dao;

import com.example.util.DbConnectionManager;

/**
 * Factory pour créer les repositories
 * Version simplifiée pour notre modèle Student
 */
public class DAOFactory {
    private final DbConnectionManager dbConnectionManager;
    private StudentRepository studentRepository;
    private UserRepository userRepository;
    
    public DAOFactory(DbConnectionManager dbConnectionManager) {
        this.dbConnectionManager = dbConnectionManager;
    }
    
    /**
     * Obtient le repository des étudiants
     */
    public StudentRepository getStudentRepository() {
        if (studentRepository == null) {
            studentRepository = new PostgresStudentRepository(dbConnectionManager);
        }
        return studentRepository;
    }
    
    /**
     * Obtient le repository des utilisateurs
     */
    public UserRepository getUserRepository() {
        if (userRepository == null) {
            userRepository = new PostgresUserRepository(dbConnectionManager);
        }
        return userRepository;
    }
}
