package com.example.export;

import com.example.model.Student;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Exportateur JSON pour les données d'étudiants
 * Version simplifiée pour le modèle Student avec une seule note
 */
public class JsonStudentExporter implements DataExporter {
    private static final Logger logger = LoggerFactory.getLogger(JsonStudentExporter.class);
    
    private final ObjectMapper objectMapper;
    
    public JsonStudentExporter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
        this.objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        this.objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
    }
    
    @Override
    public void exportStudents(List<Student> students, String filePath) throws Exception {
        if (students == null) {
            throw new IllegalArgumentException("La liste d'étudiants ne peut pas être null");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        
        logger.info("Début de l'export JSON de {} étudiants vers: {}", students.size(), filePath);
        
        try {
            // Créer la structure JSON
            Map<String, Object> exportData = new HashMap<>();
            exportData.put("export_info", createExportInfo(students));
            exportData.put("students", students.stream()
                .map(this::convertStudentToMap)
                .collect(Collectors.toList()));
            
            // Écrire le fichier JSON
            objectMapper.writeValue(new File(filePath), exportData);
            
            logger.info("Export JSON terminé avec succès: {}", filePath);
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'export JSON: {}", filePath, e);
            throw new Exception("Impossible d'exporter vers JSON: " + e.getMessage(), e);
        }
    }
    
    /**
     * Crée les informations de l'export
     */
    private Map<String, Object> createExportInfo(List<Student> students) {
        Map<String, Object> info = new HashMap<>();
        info.put("export_date", LocalDateTime.now());
        info.put("total_students", students.size());
        info.put("application", "La Plateforme Tracker");
        info.put("version", "1.0.0");
        
        // Statistiques rapides
        if (!students.isEmpty()) {
            double avgAge = students.stream().mapToInt(Student::getAge).average().orElse(0.0);
            long studentsWithGrades = students.stream()
                .filter(s -> s.getGrade() != null && !s.getGrade().trim().isEmpty())
                .count();
            
            info.put("average_age", Math.round(avgAge * 100.0) / 100.0);
            info.put("students_with_grades", studentsWithGrades);
        }
        
        return info;
    }
    
    /**
     * Convertit un étudiant en Map pour la sérialisation JSON
     */
    private Map<String, Object> convertStudentToMap(Student student) {
        Map<String, Object> studentMap = new HashMap<>();
        
        // Informations de base du modèle simplifié
        studentMap.put("id", student.getId());
        studentMap.put("first_name", student.getFirstName());
        studentMap.put("last_name", student.getLastName());
        studentMap.put("full_name", student.getFullName());
        studentMap.put("age", student.getAge());
        studentMap.put("grade", student.getGrade());
        studentMap.put("created_at", student.getCreatedAt());
        studentMap.put("updated_at", student.getUpdatedAt());
        
        return studentMap;
    }
    
    @Override
    public String getFileExtension() {
        return "json";
    }
    
    @Override
    public String getFormatDescription() {
        return "Fichier JSON (JavaScript Object Notation)";
    }
    
    @Override
    public String getMimeType() {
        return "application/json";
    }
}
