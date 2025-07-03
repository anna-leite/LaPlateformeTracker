package com.example.controller;

import com.example.AppContext;
import com.example.model.Student;
import com.example.service.StudentService;
import com.example.util.EventBus;
import com.example.event.StudentCreatedEvent;
import com.example.event.StudentUpdatedEvent;
import com.example.event.StudentDeletedEvent;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.ResourceBundle;

/**
 * Contrôleur pour la liste des étudiants (modèle simplifié)
 */
public class StudentListController implements Initializable {
    private static final Logger logger = LogManager.getLogger(StudentListController.class);
    
    // Services
    private StudentService studentService;
    
    // UI Components - Table
    @FXML private TableView<Student> studentsTable;
    @FXML private TableColumn<Student, Integer> idColumn;
    @FXML private TableColumn<Student, String> firstNameColumn;
    @FXML private TableColumn<Student, String> lastNameColumn;
    @FXML private TableColumn<Student, Integer> ageColumn;
    @FXML private TableColumn<Student, String> gradeColumn;
    @FXML private TableColumn<Student, String> createdAtColumn;
    @FXML private TableColumn<Student, Void> actionsColumn;
    
    // Search and Filter
    @FXML private TextField searchField;
    @FXML private ComboBox<String> gradeFilterCombo;
    @FXML private ComboBox<String> sortCombo;
    @FXML private CheckBox sortDescCheckbox;
    @FXML private Spinner<Integer> minAgeSpinner;
    @FXML private Spinner<Integer> maxAgeSpinner;
    
    // Statistics
    @FXML private Label totalStudentsLabel;
    @FXML private Label averageAgeLabel;
    @FXML private Label averageGradeLabel;
    
    // Pagination
    @FXML private ComboBox<Integer> pageSizeCombo;
    @FXML private Label pageInfoLabel;
    @FXML private Label totalRecordsLabel;
    @FXML private Button firstPageBtn;
    @FXML private Button prevPageBtn;
    @FXML private Button nextPageBtn;
    @FXML private Button lastPageBtn;
    
    // Buttons
    @FXML private Button addStudentBtn;
    @FXML private Button exportBtn;
    @FXML private Button importBtn;
    @FXML private Button searchBtn;
    @FXML private Button clearBtn;
    @FXML private Button refreshBtn;
    
    // Data
    private ObservableList<Student> students = FXCollections.observableArrayList();
    
    // Pagination state
    private int currentPage = 1;
    private int pageSize = 25;
    private int totalPages = 1;
    private long totalRecords = 0;
    
    // Filter state
    private String currentSearch = "";
    private String currentGradeFilter = "";
    private String currentSort = "lastName";
    private boolean sortDesc = false;
    private int minAge = 0;
    private int maxAge = 150;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        logger.info("Initializing StudentListController");
        
        // Get services
        studentService = AppContext.getInstance().getStudentService();
        
        // Setup table columns
        setupTableColumns();
        
        // Setup filter controls
        setupFilterControls();
        
        // Setup pagination controls
        setupPaginationControls();
        
        // Setup event listeners
        setupEventListeners();
        
        // Load initial data
        loadStudents();
    }
    
    private void setupTableColumns() {
        // Configure table columns
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        firstNameColumn.setCellValueFactory(new PropertyValueFactory<>("firstName"));
        lastNameColumn.setCellValueFactory(new PropertyValueFactory<>("lastName"));
        ageColumn.setCellValueFactory(new PropertyValueFactory<>("age"));
        gradeColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getGradeDisplay()));
        
        // Format created date
        createdAtColumn.setCellValueFactory(cellData -> 
            new SimpleStringProperty(cellData.getValue().getFormattedCreatedAt()));
        
        // Actions column with buttons
        actionsColumn.setCellFactory(param -> new TableCell<Student, Void>() {
            private final Button editBtn = new Button("✏️");
            private final Button deleteBtn = new Button("🗑️");
            private final Button viewBtn = new Button("👁️");
            
            {
                editBtn.setTooltip(new Tooltip("Modifier"));
                deleteBtn.setTooltip(new Tooltip("Supprimer"));
                viewBtn.setTooltip(new Tooltip("Voir détails"));
                
                editBtn.getStyleClass().add("btn-edit");
                deleteBtn.getStyleClass().add("btn-delete");
                viewBtn.getStyleClass().add("btn-view");
                
                editBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    openStudentForm(student);
                });
                
                deleteBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    deleteStudent(student);
                });
                
                viewBtn.setOnAction(e -> {
                    Student student = getTableView().getItems().get(getIndex());
                    viewStudentDetails(student);
                });
            }
            
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    var hbox = new javafx.scene.layout.HBox(5);
                    hbox.getChildren().addAll(viewBtn, editBtn, deleteBtn);
                    setGraphic(hbox);
                }
            }
        });
        
        // Set the table data
        studentsTable.setItems(students);
    }
    
    private void setupFilterControls() {
        // Grade filter
        if (gradeFilterCombo != null) {
            gradeFilterCombo.setItems(FXCollections.observableArrayList(
                "", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D", "F", "Non évalué"
            ));
            gradeFilterCombo.getSelectionModel().selectFirst();
        }
        
        // Sort options
        if (sortCombo != null) {
            sortCombo.setItems(FXCollections.observableArrayList(
                "lastName", "firstName", "age", "grade", "createdAt"
            ));
            sortCombo.setValue("lastName");
        }
        
        // Age spinners
        if (minAgeSpinner != null) {
            minAgeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 150, 0));
        }
        if (maxAgeSpinner != null) {
            maxAgeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 150, 150));
        }
    }
    
    private void setupPaginationControls() {
        // Page size options
        if (pageSizeCombo != null) {
            pageSizeCombo.setItems(FXCollections.observableArrayList(10, 25, 50, 100));
            pageSizeCombo.setValue(pageSize);
            
            // Update page size when changed
            pageSizeCombo.setOnAction(e -> {
                pageSize = pageSizeCombo.getValue();
                currentPage = 1;
                loadStudents();
            });
        }
        
        // Pagination buttons
        if (firstPageBtn != null) {
            firstPageBtn.setOnAction(e -> {
                currentPage = 1;
                loadStudents();
            });
        }
        
        if (prevPageBtn != null) {
            prevPageBtn.setOnAction(e -> {
                if (currentPage > 1) {
                    currentPage--;
                    loadStudents();
                }
            });
        }
        
        if (nextPageBtn != null) {
            nextPageBtn.setOnAction(e -> {
                if (currentPage < totalPages) {
                    currentPage++;
                    loadStudents();
                }
            });
        }
        
        if (lastPageBtn != null) {
            lastPageBtn.setOnAction(e -> {
                currentPage = totalPages;
                loadStudents();
            });
        }
    }
    
    private void setupEventListeners() {
        // Search button
        if (searchBtn != null) {
            searchBtn.setOnAction(e -> performSearch());
        }
        
        // Clear filters
        if (clearBtn != null) {
            clearBtn.setOnAction(e -> clearFilters());
        }
        
        // Refresh data
        if (refreshBtn != null) {
            refreshBtn.setOnAction(e -> loadStudents());
        }
        
        // Add student
        if (addStudentBtn != null) {
            addStudentBtn.setOnAction(e -> openStudentForm(null));
        }
        
        // Export data
        if (exportBtn != null) {
            exportBtn.setOnAction(e -> exportData());
        }
        
        // Import data
        if (importBtn != null) {
            importBtn.setOnAction(e -> importData());
        }
        
        // Search on Enter key
        if (searchField != null) {
            searchField.setOnAction(e -> performSearch());
        }
        
        // Listen to EventBus for student updates
        EventBus.getInstance().subscribe(StudentCreatedEvent.class, this::onStudentCreated);
        EventBus.getInstance().subscribe(StudentUpdatedEvent.class, this::onStudentUpdated);
        EventBus.getInstance().subscribe(StudentDeletedEvent.class, this::onStudentDeleted);
    }
    
    private void loadStudents() {
        Task<List<Student>> task = new Task<List<Student>>() {
            @Override
            protected List<Student> call() throws Exception {
                updateProgress(-1, 1); // Indeterminate progress
                
                // Apply filters
                currentSearch = searchField != null ? searchField.getText().trim() : "";
                currentGradeFilter = gradeFilterCombo != null ? gradeFilterCombo.getValue() : "";
                currentSort = sortCombo != null ? sortCombo.getValue() : "lastName";
                sortDesc = sortDescCheckbox != null ? sortDescCheckbox.isSelected() : false;
                minAge = minAgeSpinner != null ? minAgeSpinner.getValue() : 0;
                maxAge = maxAgeSpinner != null ? maxAgeSpinner.getValue() : 150;
                
                // Get filtered and paginated data
                List<Student> result;
                if (currentSearch.isEmpty() && (currentGradeFilter == null || currentGradeFilter.isEmpty()) && 
                    minAge == 0 && maxAge == 150) {
                    // No filters - use simple pagination
                    result = studentService.getStudents(currentPage, pageSize, currentSort, sortDesc);
                } else {
                    // Apply filters
                    result = studentService.searchStudents(
                        currentSearch, currentGradeFilter, minAge, maxAge,
                        currentPage, pageSize, currentSort, sortDesc
                    );
                }
                
                // Get total count for pagination
                totalRecords = studentService.getStudentCount();
                totalPages = (int) Math.ceil((double) totalRecords / pageSize);
                
                return result;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    students.clear();
                    students.addAll(getValue());
                    updatePaginationInfo();
                    updateStatistics();
                });
            }
            
            @Override
            protected void failed() {
                Platform.runLater(() -> {
                    logger.error("Failed to load students", getException());
                    showError("Erreur de chargement", 
                        "Impossible de charger les étudiants: " + getException().getMessage());
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void performSearch() {
        currentPage = 1;
        loadStudents();
    }
    
    private void clearFilters() {
        if (searchField != null) searchField.clear();
        if (gradeFilterCombo != null) gradeFilterCombo.getSelectionModel().selectFirst();
        if (minAgeSpinner != null) minAgeSpinner.getValueFactory().setValue(0);
        if (maxAgeSpinner != null) maxAgeSpinner.getValueFactory().setValue(150);
        if (sortCombo != null) sortCombo.setValue("lastName");
        if (sortDescCheckbox != null) sortDescCheckbox.setSelected(false);
        currentPage = 1;
        loadStudents();
    }
    
    private void updatePaginationInfo() {
        if (pageInfoLabel != null) {
            pageInfoLabel.setText(String.format("Page %d sur %d", currentPage, totalPages));
        }
        if (totalRecordsLabel != null) {
            totalRecordsLabel.setText(String.format("Total: %d étudiants", totalRecords));
        }
        
        if (firstPageBtn != null) firstPageBtn.setDisable(currentPage <= 1);
        if (prevPageBtn != null) prevPageBtn.setDisable(currentPage <= 1);
        if (nextPageBtn != null) nextPageBtn.setDisable(currentPage >= totalPages);
        if (lastPageBtn != null) lastPageBtn.setDisable(currentPage >= totalPages);
    }
    
    private void updateStatistics() {
        Task<Void> task = new Task<Void>() {
            private long totalStudents;
            private double averageAge;
            private double averageGrade;
            
            @Override
            protected Void call() throws Exception {
                totalStudents = studentService.getStudentCount();
                averageAge = studentService.getAverageAge();
                averageGrade = studentService.getAverageGrade();
                return null;
            }
            
            @Override
            protected void succeeded() {
                Platform.runLater(() -> {
                    if (totalStudentsLabel != null) {
                        totalStudentsLabel.setText(String.valueOf(totalStudents));
                    }
                    if (averageAgeLabel != null) {
                        averageAgeLabel.setText(String.format("%.1f ans", averageAge));
                    }
                    if (averageGradeLabel != null) {
                        averageGradeLabel.setText(String.format("%.1f/20", averageGrade));
                    }
                });
            }
        };
        
        new Thread(task).start();
    }
    
    private void openStudentForm(Student student) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/student_form.fxml"));
            Parent root = loader.load();
            
            StudentFormController controller = loader.getController();
            controller.setStudent(student);
            
            Stage stage = new Stage();
            stage.setTitle(student == null ? "Nouveau étudiant" : "Modifier étudiant");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.initOwner(studentsTable.getScene().getWindow());
            stage.showAndWait();
            
        } catch (IOException e) {
            logger.error("Failed to open student form", e);
            showError("Erreur", "Impossible d'ouvrir le formulaire étudiant");
        }
    }
    
    private void viewStudentDetails(Student student) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Détails de l'étudiant");
        alert.setHeaderText(student.getFullName());
        
        String details = String.format(
            "ID: %d\n" +
            "Nom: %s %s\n" +
            "Âge: %d ans\n" +
            "Note: %s (%s)\n" +
            "Créé le: %s\n" +
            "Modifié le: %s",
            student.getId(),
            student.getFirstName(), student.getLastName(),
            student.getAge(),
            student.getGradeDisplay(), student.getGradeMention(),
            student.getFormattedCreatedAt(),
            student.getFormattedUpdatedAt()
        );
        
        alert.setContentText(details);
        alert.showAndWait();
    }
    
    private void deleteStudent(Student student) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmer la suppression");
        alert.setHeaderText("Supprimer l'étudiant");
        alert.setContentText(String.format(
            "Êtes-vous sûr de vouloir supprimer %s ?\n\nCette action est irréversible.",
            student.getFullName()
        ));
        
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    studentService.deleteStudent(student.getId());
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        students.remove(student);
                        updateStatistics();
                        showInfo("Succès", "Étudiant supprimé avec succès");
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        logger.error("Failed to delete student", getException());
                        showError("Erreur", "Impossible de supprimer l'étudiant: " + getException().getMessage());
                    });
                }
            };
            
            new Thread(task).start();
        }
    }
    
    private void exportData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Exporter les données");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        
        File file = fileChooser.showSaveDialog(studentsTable.getScene().getWindow());
        if (file != null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() throws Exception {
                    String extension = getFileExtension(file.getName());
                    switch (extension) {
                        case "csv":
                            studentService.exportToCSV(file.getAbsolutePath());
                            break;
                        case "json":
                            studentService.exportToJSON(file.getAbsolutePath());
                            break;
                        case "xml":
                            studentService.exportToXML(file.getAbsolutePath());
                            break;
                        default:
                            throw new IllegalArgumentException("Format non supporté: " + extension);
                    }
                    return null;
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> 
                        showInfo("Succès", "Données exportées avec succès"));
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        logger.error("Failed to export data", getException());
                        showError("Erreur", "Impossible d'exporter les données: " + getException().getMessage());
                    });
                }
            };
            
            new Thread(task).start();
        }
    }
    
    private void importData() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Importer les données");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("CSV Files", "*.csv"),
            new FileChooser.ExtensionFilter("JSON Files", "*.json"),
            new FileChooser.ExtensionFilter("XML Files", "*.xml")
        );
        
        File file = fileChooser.showOpenDialog(studentsTable.getScene().getWindow());
        if (file != null) {
            Task<Integer> task = new Task<Integer>() {
                @Override
                protected Integer call() throws Exception {
                    String extension = getFileExtension(file.getName());
                    switch (extension) {
                        case "csv":
                            return studentService.importFromCSV(file.getAbsolutePath());
                        case "json":
                            return studentService.importFromJSON(file.getAbsolutePath());
                        case "xml":
                            return studentService.importFromXML(file.getAbsolutePath());
                        default:
                            throw new IllegalArgumentException("Format non supporté: " + extension);
                    }
                }
                
                @Override
                protected void succeeded() {
                    Platform.runLater(() -> {
                        int imported = getValue();
                        showInfo("Succès", String.format("%d étudiants importés avec succès", imported));
                        loadStudents();
                    });
                }
                
                @Override
                protected void failed() {
                    Platform.runLater(() -> {
                        logger.error("Failed to import data", getException());
                        showError("Erreur", "Impossible d'importer les données: " + getException().getMessage());
                    });
                }
            };
            
            new Thread(task).start();
        }
    }
    
    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        return lastDotIndex > 0 ? fileName.substring(lastDotIndex + 1).toLowerCase() : "";
    }
    
    // Event handlers
    private void onStudentCreated(StudentCreatedEvent event) {
        Platform.runLater(this::loadStudents);
    }
    
    private void onStudentUpdated(StudentUpdatedEvent event) {
        Platform.runLater(this::loadStudents);
    }
    
    private void onStudentDeleted(StudentDeletedEvent event) {
        Platform.runLater(this::loadStudents);
    }
    
    // Utility methods
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
