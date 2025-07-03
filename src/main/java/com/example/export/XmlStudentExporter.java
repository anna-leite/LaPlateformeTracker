package com.example.export;

import com.example.model.Student;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

/**
 * Exportateur XML pour les données d'étudiants
 * Version simplifiée pour le modèle Student avec une seule note
 */
public class XmlStudentExporter implements DataExporter {
    private static final Logger logger = LoggerFactory.getLogger(XmlStudentExporter.class);
    
    @Override
    public void exportStudents(List<Student> students, String filePath) throws Exception {
        if (students == null) {
            throw new IllegalArgumentException("La liste d'étudiants ne peut pas être null");
        }
        
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Le chemin du fichier ne peut pas être vide");
        }
        
        logger.info("Début de l'export XML de {} étudiants vers: {}", students.size(), filePath);
        
        try (PrintWriter writer = new PrintWriter(new FileWriter(filePath))) {
            // En-tête XML
            writer.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
            writer.println("<students>");
            
            // Exporter chaque étudiant
            for (Student student : students) {
                exportStudent(writer, student);
            }
            
            // Fermeture
            writer.println("</students>");
            
            logger.info("Export XML terminé avec succès: {}", filePath);
            
        } catch (IOException e) {
            logger.error("Erreur lors de l'export XML: {}", filePath, e);
            throw new Exception("Impossible d'exporter vers XML: " + e.getMessage(), e);
        }
    }
    
    /**
     * Exporte un étudiant individuel vers le XML
     */
    private void exportStudent(PrintWriter writer, Student student) {
        writer.println("  <student>");
        writer.printf("    <id>%d</id>%n", student.getId());
        writer.printf("    <firstName>%s</firstName>%n", escapeXml(student.getFirstName()));
        writer.printf("    <lastName>%s</lastName>%n", escapeXml(student.getLastName()));
        writer.printf("    <fullName>%s</fullName>%n", escapeXml(student.getFullName()));
        writer.printf("    <age>%d</age>%n", student.getAge());
        writer.printf("    <grade>%s</grade>%n", escapeXml(student.getGrade()));
        writer.printf("    <createdAt>%s</createdAt>%n", student.getCreatedAt());
        writer.printf("    <updatedAt>%s</updatedAt>%n", student.getUpdatedAt());
        writer.println("  </student>");
    }
    
    /**
     * Échappe les caractères spéciaux XML
     */
    private String escapeXml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                  .replace("<", "&lt;")
                  .replace(">", "&gt;")
                  .replace("\"", "&quot;")
                  .replace("'", "&apos;");
    }
    
    @Override
    public String getFileExtension() {
        return "xml";
    }
    
    @Override
    public String getFormatDescription() {
        return "Fichier XML (eXtensible Markup Language)";
    }
    
    @Override
    public String getMimeType() {
        return "application/xml";
    }
}
