package com.example.export;

import com.example.model.Student;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Exportateur CSV pour les données d'étudiants
 * Version simplifiée pour le modèle Student avec une seule note
 */
public class CsvStudentExporter implements DataExporter {
    private static final Logger logger = LoggerFactory.getLogger(CsvStudentExporter.class);
    
    // En-têtes du fichier CSV pour le modèle simplifié
    private static final String[] HEADERS = {
        "ID", "Prénom", "Nom", "Âge", "Note", 
        "Date de Création", "Date de Modification"
    };
    
    @Override
    public void exportStudents(List<Student> students, String filePath) throws Exception {
        if (students == null) {
            throw new IllegalArgumentException("La liste d'étudiants ne peut pas être null");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        
        logger.info("Début de l'export CSV de {} étudiants vers: {}", students.size(), filePath);
        
        try (FileWriter fileWriter = new FileWriter(filePath);
             CSVPrinter csvPrinter = new CSVPrinter(fileWriter, 
                 CSVFormat.DEFAULT.builder().setHeader(HEADERS).build())) {
            
            for (Student student : students) {
                exportStudent(csvPrinter, student);
            }
            
            csvPrinter.flush();
            logger.info("Export CSV terminé avec succès: {}", filePath);
            
        } catch (IOException e) {
            logger.error("Erreur lors de l'export CSV: {}", filePath, e);
            throw new Exception("Impossible d'exporter vers CSV: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exporte un étudiant individuel vers le CSV
     */
    private void exportStudent(CSVPrinter csvPrinter, Student student) throws IOException {
        // Écrire la ligne de données avec les champs simplifiés
        csvPrinter.printRecord(
            student.getId(),
            student.getFirstName(),
            student.getLastName(),
            student.getAge(),
            student.getGrade(),
            student.getCreatedAt() != null ? student.getCreatedAt().toString() : "",
            student.getUpdatedAt() != null ? student.getUpdatedAt().toString() : ""
        );
    }
    
    @Override
    public String getFileExtension() {
        return "csv";
    }
    
    @Override
    public String getFormatDescription() {
        return "Fichier CSV (Comma Separated Values)";
    }
    
    @Override
    public String getMimeType() {
        return "text/csv";
    }
}
