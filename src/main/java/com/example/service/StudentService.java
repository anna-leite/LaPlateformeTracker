package com.example.service;

import com.example.dao.StudentRepository;
import com.example.model.Student;
import com.example.util.EventBus;
import com.example.event.StudentCreatedEvent;
import com.example.event.StudentUpdatedEvent;
import com.example.event.StudentDeletedEvent;
import com.example.export.CsvStudentExporter;
import com.example.export.JsonStudentExporter;
import com.example.export.XmlStudentExporter;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Service pour la gestion des étudiants
 * Couche métier qui encapsule la logique business et utilise les repositories
 */
public class StudentService {
    private static final Logger logger = LogManager.getLogger(StudentService.class);
    
    private final StudentRepository studentRepository;
    private final EventBus eventBus;
    
    public StudentService(StudentRepository studentRepository, EventBus eventBus) {
        this.studentRepository = studentRepository;
        this.eventBus = eventBus;
    }
    
    // CRUD Operations
    
    /**
     * Crée un nouveau student
     */
    public Student createStudent(Student student) {
        logger.info("Creating new student: {} {}", student.getFirstName(), student.getLastName());
        
        // Validation
        if (!student.isValid()) {
            throw new IllegalArgumentException("Student data is invalid: " + student.getValidationErrors());
        }
        
        // Set timestamps
        LocalDateTime now = LocalDateTime.now();
        student.setCreatedAt(now);
        student.setUpdatedAt(now);
        
        // Save
        Student savedStudent = studentRepository.save(student);
        
        // Publish event
        eventBus.publish(new StudentCreatedEvent(savedStudent));
        
        logger.info("Student created successfully with ID: {}", savedStudent.getId());
        return savedStudent;
    }
    
    /**
     * Met à jour un étudiant existant
     */
    public Student updateStudent(Student student) {
        logger.info("Updating student: {}", student.getId());
        
        // Validation
        if (!student.isValid()) {
            throw new IllegalArgumentException("Student data is invalid: " + student.getValidationErrors());
        }
        
        // Check if student exists
        if (!studentRepository.existsById(student.getId())) {
            throw new IllegalArgumentException("Student not found with ID: " + student.getId());
        }
        
        // Update timestamp
        student.setUpdatedAt(LocalDateTime.now());
        
        // Save
        Student updatedStudent = studentRepository.update(student);
        
        // Publish event
        eventBus.publish(new StudentUpdatedEvent(updatedStudent));
        
        logger.info("Student updated successfully: {}", updatedStudent.getId());
        return updatedStudent;
    }
    
    /**
     * Trouve un étudiant par son ID
     */
    public Optional<Student> getStudentById(Integer id) {
        logger.debug("Finding student by ID: {}", id);
        return studentRepository.findById(id);
    }
    
    /**
     * Supprime un étudiant
     */
    public boolean deleteStudent(Integer id) {
        logger.info("Deleting student: {}", id);
        
        if (!studentRepository.existsById(id)) {
            throw new IllegalArgumentException("Student not found with ID: " + id);
        }
        
        boolean deleted = studentRepository.deleteById(id);
        
        if (deleted) {
            // Publish event
            eventBus.publish(new StudentDeletedEvent(id));
            logger.info("Student deleted successfully: {}", id);
        }
        
        return deleted;
    }
    
    // Search and Filter Operations
    
    /**
     * Récupère tous les étudiants avec pagination et tri
     */
    public List<Student> getStudents(int page, int pageSize, String sortBy, boolean sortDesc) {
        logger.debug("Getting students - page: {}, size: {}, sort: {} {}", 
                    page, pageSize, sortBy, sortDesc ? "DESC" : "ASC");
        
        int offset = (page - 1) * pageSize;
        return studentRepository.findAllSorted(sortBy, !sortDesc, offset, pageSize);
    }
    
    /**
     * Recherche des étudiants avec filtres
     */
    public List<Student> searchStudents(String searchTerm, String gradeFilter, 
                                       int minAge, int maxAge, int page, int pageSize, 
                                       String sortBy, boolean sortDesc) {
        logger.debug("Searching students with filters - term: '{}', grade: '{}', age: {}-{}", 
                    searchTerm, gradeFilter, minAge, maxAge);
        
        List<Student> results = studentRepository.findAll();
        
        // Apply filters
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String term = searchTerm.toLowerCase();
            results = results.stream()
                .filter(s -> s.getFirstName().toLowerCase().contains(term) ||
                           s.getLastName().toLowerCase().contains(term))
                .collect(Collectors.toList());
        }
        
        if (gradeFilter != null && !gradeFilter.trim().isEmpty()) {
            results = results.stream()
                .filter(s -> gradeFilter.equals(s.getGrade()))
                .collect(Collectors.toList());
        }
        
        if (minAge > 0 || maxAge < 150) {
            results = results.stream()
                .filter(s -> s.getAge() >= minAge && s.getAge() <= maxAge)
                .collect(Collectors.toList());
        }
        
        // Sort
        results = sortStudents(results, sortBy, sortDesc);
        
        // Paginate
        int offset = (page - 1) * pageSize;
        return results.stream()
                .skip(offset)
                .limit(pageSize)
                .collect(Collectors.toList());
    }
    
    private List<Student> sortStudents(List<Student> students, String sortBy, boolean sortDesc) {
        return students.stream()
            .sorted((s1, s2) -> {
                int result = 0;
                switch (sortBy) {
                    case "firstName":
                        result = s1.getFirstName().compareTo(s2.getFirstName());
                        break;
                    case "lastName":
                        result = s1.getLastName().compareTo(s2.getLastName());
                        break;
                    case "age":
                        result = Integer.compare(s1.getAge(), s2.getAge());
                        break;
                    case "grade":
                        result = Double.compare(s1.getNumericGrade(), s2.getNumericGrade());
                        break;
                    case "createdAt":
                        result = s1.getCreatedAt().compareTo(s2.getCreatedAt());
                        break;
                    default:
                        result = s1.getLastName().compareTo(s2.getLastName());
                }
                return sortDesc ? -result : result;
            })
            .collect(Collectors.toList());
    }
    
    // Statistics
    
    /**
     * Compte le nombre total d'étudiants
     */
    public long getStudentCount() {
        return studentRepository.count();
    }
    
    /**
     * Calcule l'âge moyen des étudiants
     */
    public double getAverageAge() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return 0.0;
        }
        
        return students.stream()
                .mapToInt(Student::getAge)
                .average()
                .orElse(0.0);
    }
    
    /**
     * Calcule la note moyenne des étudiants
     */
    public double getAverageGrade() {
        List<Student> students = studentRepository.findAll();
        if (students.isEmpty()) {
            return 0.0;
        }
        
        return students.stream()
                .mapToDouble(Student::getNumericGrade)
                .filter(grade -> grade >= 0) // Exclude invalid grades
                .average()
                .orElse(0.0);
    }
    
    // Import/Export Operations
    
    /**
     * Exporte les étudiants au format CSV
     */
    public void exportToCSV(String filePath) throws Exception {
        logger.info("Exporting students to CSV: {}", filePath);
        
        List<Student> students = studentRepository.findAll();
        CsvStudentExporter exporter = new CsvStudentExporter();
        exporter.exportStudents(students, filePath);
        
        logger.info("Exported {} students to CSV", students.size());
    }
    
    /**
     * Exporte les étudiants au format JSON
     */
    public void exportToJSON(String filePath) throws Exception {
        logger.info("Exporting students to JSON: {}", filePath);
        
        List<Student> students = studentRepository.findAll();
        JsonStudentExporter exporter = new JsonStudentExporter();
        exporter.exportStudents(students, filePath);
        
        logger.info("Exported {} students to JSON", students.size());
    }
    
    /**
     * Exporte les étudiants au format XML
     */
    public void exportToXML(String filePath) throws Exception {
        logger.info("Exporting students to XML: {}", filePath);
        
        List<Student> students = studentRepository.findAll();
        XmlStudentExporter exporter = new XmlStudentExporter();
        exporter.exportStudents(students, filePath);
        
        logger.info("Exported {} students to XML", students.size());
    }
    
    /**
     * Importe des étudiants depuis un fichier CSV
     */
    public int importFromCSV(String filePath) throws IOException {
        logger.info("Importing students from CSV: {}", filePath);
        
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        if (lines.isEmpty()) {
            return 0;
        }
        
        // Skip header
        int imported = 0;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i);
            if (line.trim().isEmpty()) continue;
            
            try {
                Student student = parseCSVLine(line);
                if (student != null && student.isValid()) {
                    createStudent(student);
                    imported++;
                }
            } catch (Exception e) {
                logger.warn("Failed to import line {}: {}", i + 1, e.getMessage());
            }
        }
        
        logger.info("Imported {} students from CSV", imported);
        return imported;
    }
    
    /**
     * Importe des étudiants depuis un fichier JSON
     */
    public int importFromJSON(String filePath) throws IOException {
        logger.info("Importing students from JSON: {}", filePath);
        
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        
        Student[] students = mapper.readValue(new File(filePath), Student[].class);
        
        int imported = 0;
        for (Student student : students) {
            try {
                if (student.isValid()) {
                    // Clear ID for new import
                    student.setId(0);
                    createStudent(student);
                    imported++;
                }
            } catch (Exception e) {
                logger.warn("Failed to import student: {}", e.getMessage());
            }
        }
        
        logger.info("Imported {} students from JSON", imported);
        return imported;
    }
    
    /**
     * Importe des étudiants depuis un fichier XML
     */
    public int importFromXML(String filePath) throws IOException {
        logger.info("Importing students from XML: {}", filePath);
        
        // Simple XML parsing for now
        // In a real application, you would use a proper XML parser
        List<String> lines = Files.readAllLines(Paths.get(filePath));
        
        int imported = 0;
        Student currentStudent = null;
        
        for (String line : lines) {
            line = line.trim();
            
            if (line.equals("<student>")) {
                currentStudent = new Student();
            } else if (line.equals("</student>") && currentStudent != null) {
                if (currentStudent.isValid()) {
                    try {
                        createStudent(currentStudent);
                        imported++;
                    } catch (Exception e) {
                        logger.warn("Failed to import student: {}", e.getMessage());
                    }
                }
                currentStudent = null;
            } else if (currentStudent != null) {
                parseXMLElement(line, currentStudent);
            }
        }
        
        logger.info("Imported {} students from XML", imported);
        return imported;
    }
    
    // Helper methods
    
    private Student parseCSVLine(String line) {
        // Simple CSV parsing - in production, use a proper CSV library
        String[] parts = line.split(",");
        if (parts.length < 4) {
            return null;
        }
        
        try {
            String firstName = parts[1].replace("\"", "").trim();
            String lastName = parts[2].replace("\"", "").trim();
            int age = Integer.parseInt(parts[3].trim());
            String grade = parts.length > 4 ? parts[4].replace("\"", "").trim() : "";
            
            return new Student(firstName, lastName, age, grade);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    private void parseXMLElement(String line, Student student) {
        if (line.startsWith("<firstName>") && line.endsWith("</firstName>")) {
            String value = line.substring(11, line.length() - 12);
            student.setFirstName(unescapeXml(value));
        } else if (line.startsWith("<lastName>") && line.endsWith("</lastName>")) {
            String value = line.substring(10, line.length() - 11);
            student.setLastName(unescapeXml(value));
        } else if (line.startsWith("<age>") && line.endsWith("</age>")) {
            String value = line.substring(5, line.length() - 6);
            try {
                student.setAge(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                // Ignore invalid age
            }
        } else if (line.startsWith("<grade>") && line.endsWith("</grade>")) {
            String value = line.substring(7, line.length() - 8);
            student.setGrade(unescapeXml(value));
        }
    }
    
    private String unescapeXml(String text) {
        if (text == null) return "";
        return text.replace("&amp;", "&")
                  .replace("&lt;", "<")
                  .replace("&gt;", ">")
                  .replace("&quot;", "\"")
                  .replace("&apos;", "'");
    }
}
