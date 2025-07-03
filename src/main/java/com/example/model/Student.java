package com.example.model;

import javafx.beans.property.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Objects;

/**
 * Modèle Student
 */
public class Student {
    private final IntegerProperty id = new SimpleIntegerProperty();
    private final StringProperty firstName = new SimpleStringProperty();
    private final StringProperty lastName = new SimpleStringProperty();
    private final IntegerProperty age = new SimpleIntegerProperty();
    private final StringProperty grade = new SimpleStringProperty();
    private final ObjectProperty<LocalDateTime> createdAt = new SimpleObjectProperty<>();
    private final ObjectProperty<LocalDateTime> updatedAt = new SimpleObjectProperty<>();

    // Constructeurs
    public Student() {}

    public Student(String firstName, String lastName, int age, String grade) {
        setFirstName(firstName);
        setLastName(lastName);
        setAge(age);
        setGrade(grade);
        setCreatedAt(LocalDateTime.now());
        setUpdatedAt(LocalDateTime.now());
    }

    public Student(int id, String firstName, String lastName, int age, String grade, 
                   LocalDateTime createdAt, LocalDateTime updatedAt) {
        setId(id);
        setFirstName(firstName);
        setLastName(lastName);
        setAge(age);
        setGrade(grade);
        setCreatedAt(createdAt);
        setUpdatedAt(updatedAt);
    }

    // Getters et setters pour les propriétés JavaFX
    public IntegerProperty idProperty() { return id; }
    public int getId() { return id.get(); }
    public void setId(int id) { this.id.set(id); }

    public StringProperty firstNameProperty() { return firstName; }
    public String getFirstName() { return firstName.get(); }
    public void setFirstName(String firstName) { this.firstName.set(firstName); }

    public StringProperty lastNameProperty() { return lastName; }
    public String getLastName() { return lastName.get(); }
    public void setLastName(String lastName) { this.lastName.set(lastName); }

    public IntegerProperty ageProperty() { return age; }
    public int getAge() { return age.get(); }
    public void setAge(int age) { this.age.set(age); }

    public StringProperty gradeProperty() { return grade; }
    public String getGrade() { return grade.get(); }
    public void setGrade(String grade) { this.grade.set(grade); }

    public ObjectProperty<LocalDateTime> createdAtProperty() { return createdAt; }
    public LocalDateTime getCreatedAt() { return createdAt.get(); }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt.set(createdAt); }

    public ObjectProperty<LocalDateTime> updatedAtProperty() { return updatedAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt.get(); }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt.set(updatedAt); }

    // Méthodes utilitaires pour la note
    public String getFullName() {
        return firstName.get() + " " + lastName.get();
    }

    public String getGradeDisplay() {
        String grade = getGrade();
        if (grade == null || grade.trim().isEmpty()) {
            return "Non évalué";
        }
        return grade;
    }

    public double getNumericGrade() {
        String grade = getGrade();
        if (grade == null || grade.trim().isEmpty()) {
            return -1;
        }
        
        // Conversion note lettre -> numérique
        switch (grade.toUpperCase()) {
            case "A": return 18.0;
            case "A-": return 16.0;
            case "B+": return 14.0;
            case "B": return 12.0;
            case "B-": return 10.0;
            case "C+": return 8.0;
            case "C": return 6.0;
            case "C-": return 4.0;
            case "D": return 2.0;
            case "F": return 0.0;
            default:
                try {
                    return Double.parseDouble(grade);
                } catch (NumberFormatException e) {
                    return -1;
                }
        }
    }

    public String getGradeMention() {
        double numericGrade = getNumericGrade();
        if (numericGrade == -1) return "Non évalué";
        if (numericGrade >= 16) return "Très Bien";
        if (numericGrade >= 14) return "Bien";
        if (numericGrade >= 12) return "Assez Bien";
        if (numericGrade >= 10) return "Passable";
        return "Insuffisant";
    }

    public String getFormattedCreatedAt() {
        return createdAt.get() != null ? 
            createdAt.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public String getFormattedUpdatedAt() {
        return updatedAt.get() != null ? 
            updatedAt.get().format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")) : "";
    }

    public void updateTimestamp() {
        setUpdatedAt(LocalDateTime.now());
    }

    public boolean isValid() {
        return firstName.get() != null && !firstName.get().trim().isEmpty() &&
               lastName.get() != null && !lastName.get().trim().isEmpty() &&
               age.get() > 0 && age.get() < 150;
    }

    public String getValidationErrors() {
        StringBuilder errors = new StringBuilder();
        
        if (firstName.get() == null || firstName.get().trim().isEmpty()) {
            errors.append("Le prénom est requis.\n");
        } else if (firstName.get().length() > 100) {
            errors.append("Le prénom ne peut pas dépasser 100 caractères.\n");
        }
        
        if (lastName.get() == null || lastName.get().trim().isEmpty()) {
            errors.append("Le nom est requis.\n");
        } else if (lastName.get().length() > 100) {
            errors.append("Le nom ne peut pas dépasser 100 caractères.\n");
        }
        
        if (age.get() <= 0) {
            errors.append("L'âge doit être supérieur à 0.\n");
        } else if (age.get() >= 150) {
            errors.append("L'âge doit être inférieur à 150.\n");
        }
        
        if (grade.get() != null && grade.get().length() > 10) {
            errors.append("La note ne peut pas dépasser 10 caractères.\n");
        }
        
        return errors.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Student student = (Student) o;
        return getId() == student.getId();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId());
    }

    @Override
    public String toString() {
        return "Student{" +
                "id=" + getId() +
                ", firstName='" + getFirstName() + '\'' +
                ", lastName='" + getLastName() + '\'' +
                ", age=" + getAge() +
                ", grade='" + getGrade() + '\'' +
                ", createdAt=" + getCreatedAt() +
                ", updatedAt=" + getUpdatedAt() +
                '}';
    }
}
