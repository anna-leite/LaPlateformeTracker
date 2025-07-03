package com.example.controller;

import com.example.AppContext;
import com.example.model.Student;
import com.example.service.StudentService;
import com.example.util.EventBus;
import com.example.event.StudentCreatedEvent;
import com.example.event.StudentUpdatedEvent;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour le formulaire d'ajout/modification d'étudiant
 */
public class StudentFormController implements Initializable {
    private static final Logger logger = LogManager.getLogger(StudentFormController.class);
    
    // Services
    private StudentService studentService;
    
    // UI Components
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private Spinner<Integer> ageSpinner;
    @FXML private ComboBox<String> gradeCombo;
    @FXML private Button saveBtn;
    @FXML private Button cancelBtn;
    @FXML private Label titleLabel;
    
    // Form state
    private Student student; // null for new student, existing student for edit
    private boolean isEditMode = false;
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing StudentFormController");
        
        // Get services
        studentService = AppContext.getInstance().getStudentService();
        
        // Setup form controls
        setupFormControls();
        
        // Setup event listeners
        setupEventListeners();
    }
    
    private void setupFormControls() {
        // Age spinner
        ageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 150, 18));
        ageSpinner.setEditable(true);
        
        // Grade combo
        gradeCombo.getItems().addAll(
            "", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F"
        );
        gradeCombo.getSelectionModel().selectFirst();
        
        // Form validation
        setupValidation();
    }
    
    private void setupValidation() {
        // Real-time validation
        firstNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        lastNameField.textProperty().addListener((obs, oldVal, newVal) -> validateForm());
        ageSpinner.valueProperty().addListener((obs, oldVal, newVal) -> validateForm());
        
        // Initial validation
        validateForm();
    }
    
    private void setupEventListeners() {
        // Save button
        saveBtn.setOnAction(e -> handleSave());
        
        // Cancel button
        cancelBtn.setOnAction(e -> handleCancel());
        
        // Enter key on text fields
        firstNameField.setOnAction(e -> handleSave());
        lastNameField.setOnAction(e -> handleSave());
    }
    
    /**
     * Set the student to edit (null for new student)
     */
    public void setStudent(Student student) {
        this.student = student;
        this.isEditMode = (student != null);
        
        if (isEditMode) {
            titleLabel.setText("Modifier l'étudiant");
            populateForm();
        } else {
            titleLabel.setText("Nouveau étudiant");
            clearForm();
        }
        
        validateForm();
    }
    
    private void populateForm() {
        if (student != null) {
            firstNameField.setText(student.getFirstName());
            lastNameField.setText(student.getLastName());
            ageSpinner.getValueFactory().setValue(student.getAge());
            gradeCombo.setValue(student.getGrade());
        }
    }
    
    private void clearForm() {
        firstNameField.clear();
        lastNameField.clear();
        ageSpinner.getValueFactory().setValue(18);
        gradeCombo.getSelectionModel().selectFirst();
    }
    
    private void validateForm() {
        boolean isValid = true;
        StringBuilder errors = new StringBuilder();
        
        // Validate first name
        String firstName = firstNameField.getText().trim();
        if (firstName.isEmpty()) {
            errors.append("Le prénom est requis.\n");
            isValid = false;
        } else if (firstName.length() > 100) {
            errors.append("Le prénom ne peut pas dépasser 100 caractères.\n");
            isValid = false;
        }
        
        // Validate last name
        String lastName = lastNameField.getText().trim();
        if (lastName.isEmpty()) {
            errors.append("Le nom est requis.\n");
            isValid = false;
        } else if (lastName.length() > 100) {
            errors.append("Le nom ne peut pas dépasser 100 caractères.\n");
            isValid = false;
        }
        
        // Validate age
        Integer age = ageSpinner.getValue();
        if (age == null || age <= 0) {
            errors.append("L'âge doit être supérieur à 0.\n");
            isValid = false;
        } else if (age >= 150) {
            errors.append("L'âge doit être inférieur à 150.\n");
            isValid = false;
        }
        
        // Enable/disable save button
        saveBtn.setDisable(!isValid);
        
        // Show validation tooltip if errors
        if (!isValid && errors.length() > 0) {
            saveBtn.setTooltip(new Tooltip(errors.toString().trim()));
        } else {
            saveBtn.setTooltip(null);
        }
    }
    
    @FXML
    private void handleSave() {
        if (saveBtn.isDisabled()) {
            return;
        }
        
        // Create or update student
        Student studentToSave;
        if (isEditMode) {
            studentToSave = student;
            studentToSave.setFirstName(firstNameField.getText().trim());
            studentToSave.setLastName(lastNameField.getText().trim());
            studentToSave.setAge(ageSpinner.getValue());
            studentToSave.setGrade(gradeCombo.getValue());
            studentToSave.updateTimestamp();
        } else {
            studentToSave = new Student(
                firstNameField.getText().trim(),
                lastNameField.getText().trim(),
                ageSpinner.getValue(),
                gradeCombo.getValue()
            );
        }
        
        // Validate the student object
        if (!studentToSave.isValid()) {
            showError("Erreur de validation", studentToSave.getValidationErrors());
            return;
        }
        
        // Save in background
        saveStudentAsync(studentToSave);
    }
    
    private void saveStudentAsync(Student studentToSave) {
        // Disable form during save
        setFormEnabled(false);
        
        Task<Student> task = new Task<Student>() {
            @Override
            protected Student call() throws Exception {
                if (isEditMode) {
                    return studentService.updateStudent(studentToSave);
                } else {
                    return studentService.createStudent(studentToSave);
                }
            }
            
            @Override
            protected void succeeded() {
                javafx.application.Platform.runLater(() -> {
                    Student savedStudent = getValue();
                    logger.info("Student saved successfully: {}", savedStudent.getId());
                    
                    // Notify listeners
                    if (isEditMode) {
                        EventBus.getInstance().publish(new StudentUpdatedEvent(savedStudent));
                    } else {
                        EventBus.getInstance().publish(new StudentCreatedEvent(savedStudent));
                    }
                    
                    // Show success message
                    showInfo("Succès", 
                        isEditMode ? "Étudiant modifié avec succès" : "Étudiant créé avec succès");
                    
                    // Close form
                    closeForm();
                });
            }
            
            @Override
            protected void failed() {
                javafx.application.Platform.runLater(() -> {
                    setFormEnabled(true);
                    logger.error("Failed to save student", getException());
                    showError("Erreur de sauvegarde", 
                        "Impossible de sauvegarder l'étudiant: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    @FXML
    private void handleCancel() {
        // Check if form has unsaved changes
        if (hasUnsavedChanges()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Confirmer l'annulation");
            alert.setHeaderText("Annuler les modifications");
            alert.setContentText("Vous avez des modifications non sauvegardées. Voulez-vous vraiment annuler ?");
            
            if (alert.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) {
                return;
            }
        }
        
        closeForm();
    }
    
    private boolean hasUnsavedChanges() {
        if (!isEditMode) {
            // New student - check if any field is filled
            return !firstNameField.getText().trim().isEmpty() ||
                   !lastNameField.getText().trim().isEmpty() ||
                   !ageSpinner.getValue().equals(18) ||
                   !gradeCombo.getValue().isEmpty();
        } else {
            // Edit mode - check if any field has changed
            return !firstNameField.getText().trim().equals(student.getFirstName()) ||
                   !lastNameField.getText().trim().equals(student.getLastName()) ||
                   !ageSpinner.getValue().equals(student.getAge()) ||
                   !gradeCombo.getValue().equals(student.getGrade());
        }
    }
    
    private void setFormEnabled(boolean enabled) {
        firstNameField.setDisable(!enabled);
        lastNameField.setDisable(!enabled);
        ageSpinner.setDisable(!enabled);
        gradeCombo.setDisable(!enabled);
        saveBtn.setDisable(!enabled);
        cancelBtn.setDisable(!enabled);
    }
    
    private void closeForm() {
        Stage stage = (Stage) saveBtn.getScene().getWindow();
        stage.close();
    }
    
    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
