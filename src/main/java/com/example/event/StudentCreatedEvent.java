package com.example.event;

import com.example.model.Student;

/**
 * Événement déclenché quand un étudiant est créé
 */
public class StudentCreatedEvent {
    private final Student student;
    
    public StudentCreatedEvent(Student student) {
        this.student = student;
    }
    
    public Student getStudent() {
        return student;
    }
}
