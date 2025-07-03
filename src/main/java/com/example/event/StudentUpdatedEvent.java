package com.example.event;

import com.example.model.Student;

/**
 * Événement déclenché quand un étudiant est mis à jour
 */
public class StudentUpdatedEvent {
    private final Student student;
    
    public StudentUpdatedEvent(Student student) {
        this.student = student;
    }
    
    public Student getStudent() {
        return student;
    }
}
