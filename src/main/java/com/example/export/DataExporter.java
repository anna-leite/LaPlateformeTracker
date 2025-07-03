package com.example.export;

import com.example.model.Student;
import java.util.List;

/**
 * Interface pour l'exportation de données d'étudiants
 * Permet d'implémenter différents formats d'export (CSV, JSON, XML, etc.)
 */
public interface DataExporter {
    
    /**
     * Exporte une liste d'étudiants vers un fichier
     * @param students Liste des étudiants à exporter
     * @param filePath Chemin du fichier de destination
     * @throws Exception si l'export échoue
     */
    void exportStudents(List<Student> students, String filePath) throws Exception;
    
    /**
     * Retourne l'extension de fichier recommandée pour ce format
     * @return Extension de fichier (ex: "csv", "json", "xml")
     */
    String getFileExtension();
    
    /**
     * Retourne la description du format d'export
     * @return Description du format
     */
    String getFormatDescription();
    
    /**
     * Retourne le type MIME du format
     * @return Type MIME
     */
    String getMimeType();
}
