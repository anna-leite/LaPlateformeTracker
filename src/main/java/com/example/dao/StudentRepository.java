package com.example.dao;

import com.example.model.Student;
import java.util.List;
import java.util.Optional;

/**
 * Interface définissant les opérations CRUD pour les étudiants
 * Cette interface suit le pattern Repository pour l'abstraction des données
 */
public interface StudentRepository {
    
    /**
     * Sauvegarde un nouvel étudiant en base de données
     * @param student L'étudiant à sauvegarder
     * @return L'étudiant avec son ID généré
     */
    Student save(Student student);
    
    /**
     * Met à jour un étudiant existant
     * @param student L'étudiant à mettre à jour
     * @return L'étudiant mis à jour
     */
    Student update(Student student);
    
    /**
     * Trouve un étudiant par son ID
     * @param id L'ID de l'étudiant
     * @return Optional contenant l'étudiant si trouvé
     */
    Optional<Student> findById(Integer id);
    
    /**
     * Récupère tous les étudiants
     * @return Liste de tous les étudiants
     */
    List<Student> findAll();
    
    /**
     * Récupère les étudiants avec pagination
     * @param offset Décalage (nombre d'éléments à ignorer)
     * @param limit Nombre maximum d'éléments à retourner
     * @return Liste paginée des étudiants
     */
    List<Student> findAll(int offset, int limit);
    
    /**
     * Recherche des étudiants par nom (prénom ou nom de famille)
     * @param name Le nom à rechercher (partiel autorisé)
     * @return Liste des étudiants correspondants
     */
    List<Student> findByName(String name);
    
    /**
     * Recherche des étudiants par âge
     * @param minAge Âge minimum (inclus)
     * @param maxAge Âge maximum (inclus)
     * @return Liste des étudiants dans la tranche d'âge
     */
    List<Student> findByAgeRange(int minAge, int maxAge);
    
    /**
     * Recherche des étudiants par note
     * @param grade La note à rechercher (A+, A, A-, B+, B, B-, C+, C, C-, D, F)
     * @return Liste des étudiants ayant cette note
     */
    List<Student> findByGrade(String grade);
    
    /**
     * Recherche des étudiants par plage de notes numériques
     * @param minGrade Note numérique minimum
     * @param maxGrade Note numérique maximum
     * @return Liste des étudiants dans cette plage
     */
    List<Student> findByGradeRange(double minGrade, double maxGrade);
    
    /**
     * Récupère les étudiants triés selon un critère
     * @param sortBy Critère de tri (nom, prénom, âge, moyenne)
     * @param ascending true pour tri croissant, false pour décroissant
     * @return Liste triée des étudiants
     */
    List<Student> findAllSorted(String sortBy, boolean ascending);
    
    /**
     * Récupère les étudiants triés avec pagination
     * @param sortBy Critère de tri
     * @param ascending Direction du tri
     * @param offset Décalage
     * @param limit Limite
     * @return Liste triée et paginée des étudiants
     */
    List<Student> findAllSorted(String sortBy, boolean ascending, int offset, int limit);
    
    /**
     * Compte le nombre total d'étudiants
     * @return Nombre total d'étudiants
     */
    long count();
    
    /**
     * Supprime un étudiant par son ID
     * @param id L'ID de l'étudiant à supprimer
     * @return true si la suppression a réussi, false sinon
     */
    boolean deleteById(Integer id);
    
    /**
     * Vérifie si un étudiant existe par son ID
     * @param id L'ID à vérifier
     * @return true si l'étudiant existe, false sinon
     */
    boolean existsById(Integer id);
}
