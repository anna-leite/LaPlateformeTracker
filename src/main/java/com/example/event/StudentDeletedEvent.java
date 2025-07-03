package com.example.event;

/**
 * Événement déclenché quand un étudiant est supprimé
 */
public class StudentDeletedEvent {
    private final Integer studentId;
    
    public StudentDeletedEvent(Integer studentId) {
        this.studentId = studentId;
    }
    
    public Integer getStudentId() {
        return studentId;
    }
}
