package com.example.controller;

import com.example.AppContext;
import com.example.model.Student;
import com.example.service.StudentService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

/**
 * Contrôleur principal du dashboard
 * Gère la navigation et affiche les statistiques générales
 */
public class DashboardController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class);
    
    @FXML private Label userLabel;
    @FXML private Button logoutButton;
    @FXML private Button studentsButton;
    @FXML private Button usersButton;
    @FXML private Button statisticsButton;
    @FXML private Button backupButton;
    @FXML private Button exportButton;
    @FXML private Button settingsButton;
    @FXML private StackPane contentArea;
    @FXML private Label statusLabel;
    @FXML private Label connectionStatusLabel;
    
    // Tuiles du dashboard
    @FXML private Label totalStudentsLabel;
    @FXML private Label totalUsersLabel;
    @FXML private Label averageGradeLabel;
    @FXML private Label lastBackupLabel;
    @FXML private Label topStudentLabel;
    @FXML private Label systemStatusLabel;
    
    private StudentService studentService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // Obtenir les services (version simplifiée)
        AppContext context = AppContext.getInstance();
        studentService = context.getStudentService();
        
        // Configurer l'interface
        setupUserInterface();
        
        // Charger les statistiques
        loadDashboardData();
        
        logger.debug("Contrôleur du dashboard initialisé");
    }
    
    /**
     * Configure l'interface utilisateur (version simplifiée)
     */
    private void setupUserInterface() {
        // Version simplifiée sans authentification
        userLabel.setText("La Plateforme Tracker");
        
        // Masquer les fonctionnalités non implémentées
        usersButton.setVisible(false);
        backupButton.setVisible(false);
        
        // Statut initial
        statusLabel.setText("Tableau de bord chargé");
        
        // Vérifier la connexion à la base de données
        updateConnectionStatus();
    }
    
    /**
     * Charge les données du dashboard (version simplifiée)
     */
    private void loadDashboardData() {
        Task<Void> loadTask = new Task<Void>() {
            @Override
            protected Void call() throws Exception {
                try {
                    // Charger les statistiques des étudiants
                    long totalStudents = studentService.getStudentCount();
                    double avgGrade = studentService.getAverageGrade();
                    
                    Platform.runLater(() -> {
                        // Mettre à jour les tuiles
                        totalStudentsLabel.setText(String.valueOf(totalStudents));
                        totalUsersLabel.setText("N/A"); // Pas d'utilisateurs dans la version simplifiée
                        
                        if (avgGrade > 0) {
                            averageGradeLabel.setText(String.format("%.2f", avgGrade));
                        } else {
                            averageGradeLabel.setText("N/A");
                        }
                        
                        // Pas de sauvegarde dans la version simplifiée
                        lastBackupLabel.setText("N/A");
                        
                        // Meilleur étudiant
                        updateTopStudent();
                        
                        statusLabel.setText("Données chargées");
                    });
                    
                } catch (Exception e) {
                    logger.error("Erreur lors du chargement des données du dashboard", e);
                    Platform.runLater(() -> {
                        statusLabel.setText("Erreur de chargement des données");
                        showError("Impossible de charger les données: " + e.getMessage());
                    });
                }
                return null;
            }
        };
        
        Thread loadThread = new Thread(loadTask);
        loadThread.setDaemon(true);
        loadThread.start();
    }
    
    /**
     * Met à jour l'affichage du meilleur étudiant (version simplifiée)
     */
    private void updateTopStudent() {
        try {
            List<Student> students = studentService.getStudents(1, 10, "grade", true); // Tri par note décroissant
            if (!students.isEmpty()) {
                Student topStudent = students.get(0);
                if (topStudent.getGrade() != null && !topStudent.getGrade().trim().isEmpty()) {
                    topStudentLabel.setText(topStudent.getFirstName() + " " + 
                        topStudent.getLastName().charAt(0) + ".");
                } else {
                    topStudentLabel.setText("N/A");
                }
            } else {
                topStudentLabel.setText("Aucun");
            }
        } catch (Exception e) {
            logger.warn("Erreur lors de la recherche du meilleur étudiant", e);
            topStudentLabel.setText("N/A");
        }
    }
    
    /**
     * Met à jour le statut de connexion à la base de données
     */
    private void updateConnectionStatus() {
        Task<Boolean> connectionTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return AppContext.getInstance().getDbConnectionManager().testConnection();
            }
            
            @Override
            protected void succeeded() {
                boolean connected = getValue();
                Platform.runLater(() -> {
                    if (connected) {
                        connectionStatusLabel.setText("Base de données: Connectée");
                        connectionStatusLabel.setStyle("-fx-text-fill: green;");
                        systemStatusLabel.setText("En Ligne");
                    } else {
                        connectionStatusLabel.setText("Base de données: Déconnectée");
                        connectionStatusLabel.setStyle("-fx-text-fill: red;");
                        systemStatusLabel.setText("Hors Ligne");
                    }
                });
            }
        };
        
        Thread connectionThread = new Thread(connectionTask);
        connectionThread.setDaemon(true);
        connectionThread.start();
    }
    
    // Gestionnaires d'événements pour la navigation
    
    @FXML
    private void handleLogout() {
        try {
            // Version simplifiée : retourner directement à la liste des étudiants
            showStudents();
            
            logger.info("Retour à la gestion des étudiants");
            
        } catch (Exception e) {
            logger.error("Erreur lors du retour", e);
            showError("Erreur: " + e.getMessage());
        }
    }
    
    @FXML
    private void showStudents() {
        statusLabel.setText("Chargement de la gestion des étudiants...");
        loadView("/views/student_list.fxml", "Gestion des Étudiants");
    }
    
    @FXML
    private void showUsers() {
        // Fonctionnalité désactivée dans la version simplifiée
        showInfo("Gestion des utilisateurs non disponible dans la version simplifiée");
    }
    
    @FXML
    private void showStatistics() {
        statusLabel.setText("Chargement des statistiques...");
        // TODO: Implémenter la vue des statistiques
        showInfo("Fonctionnalité des statistiques en cours de développement");
    }
    
    @FXML
    private void showBackup() {
        // Fonctionnalité désactivée dans la version simplifiée
        showInfo("Gestion des sauvegardes non disponible dans la version simplifiée");
    }
    
    @FXML
    private void showExport() {
        statusLabel.setText("Chargement de l'export/import...");
        // TODO: Implémenter la vue d'export/import
        showInfo("Fonctionnalité d'export/import en cours de développement");
    }
    
    @FXML
    private void showSettings() {
        statusLabel.setText("Chargement des paramètres...");
        // TODO: Implémenter la vue des paramètres
        showInfo("Fonctionnalité des paramètres en cours de développement");
    }
    
    /**
     * Charge une vue dans la zone de contenu principale
     */
    private void loadView(String fxmlPath, String viewName) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            javafx.scene.Node view = loader.load();
            
            contentArea.getChildren().clear();
            contentArea.getChildren().add(view);
            
            statusLabel.setText(viewName + " chargé");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la vue: " + fxmlPath, e);
            showError("Impossible de charger " + viewName + ": " + e.getMessage());
            statusLabel.setText("Erreur de chargement");
        }
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Erreur");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Affiche un message d'information
     */
    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
    
    /**
     * Rafraîchit les données du dashboard
     */
    public void refreshDashboard() {
        loadDashboardData();
        updateConnectionStatus();
    }
}
