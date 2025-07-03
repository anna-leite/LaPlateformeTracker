package com.example.controller;

import com.example.App;
import com.example.AppContext;
import com.example.service.AuthService;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Contrôleur pour l'écran de connexion
 * Gère l'authentification des utilisateurs
 */
public class LoginController implements Initializable {
    private static final Logger logger = LoggerFactory.getLogger(LoginController.class);
    
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private Button loginButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;
    
    private AuthService authService;
    
    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        // TODO: Authentication not implemented in simplified version
        // authService = AppContext.getInstance().getAuthService();
        
        // Configurer les champs
        setupFields();
        
        // Focus sur le champ nom d'utilisateur
        Platform.runLater(() -> usernameField.requestFocus());
        
        logger.debug("Contrôleur de connexion initialisé");
    }
    
    /**
     * Configure les champs de saisie
     */
    private void setupFields() {
        // Valeurs par défaut pour les tests
        usernameField.setText("admin");
        passwordField.setText("admin123");
        
        // Gérer l'appui sur Entrée dans le champ mot de passe
        passwordField.setOnAction(event -> handleLogin());
        
        // Effacer le message d'erreur quand l'utilisateur tape
        usernameField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
        passwordField.textProperty().addListener((obs, oldVal, newVal) -> clearError());
    }
    
    /**
     * Gère la tentative de connexion
     */
    @FXML
    private void handleLogin() {
        String username = usernameField.getText().trim();
        String password = passwordField.getText();
        
        // Validation des champs
        if (username.isEmpty()) {
            showError("Veuillez saisir un nom d'utilisateur");
            usernameField.requestFocus();
            return;
        }
        
        if (password.isEmpty()) {
            showError("Veuillez saisir un mot de passe");
            passwordField.requestFocus();
            return;
        }
        
        // Désactiver les contrôles pendant l'authentification
        setControlsEnabled(false);
        clearError();
        
        // Effectuer l'authentification en arrière-plan
        Task<Boolean> loginTask = new Task<Boolean>() {
            @Override
            protected Boolean call() throws Exception {
                return authService.login(username, password);
            }
            
            @Override
            protected void succeeded() {
                setControlsEnabled(true);
                
                if (getValue()) {
                    logger.info("Connexion réussie pour l'utilisateur: {}", username);
                    showMainApplication();
                } else {
                    showError("Nom d'utilisateur ou mot de passe incorrect");
                    passwordField.clear();
                    usernameField.requestFocus();
                }
            }
            
            @Override
            protected void failed() {
                setControlsEnabled(true);
                Throwable exception = getException();
                logger.error("Erreur lors de l'authentification", exception);
                showError("Erreur de connexion: " + exception.getMessage());
                passwordField.clear();
                usernameField.requestFocus();
            }
        };
        
        Thread loginThread = new Thread(loginTask);
        loginThread.setDaemon(true);
        loginThread.start();
    }
    
    /**
     * Gère l'annulation de la connexion
     */
    @FXML
    private void handleCancel() {
        logger.info("Connexion annulée par l'utilisateur");
        Platform.exit();
    }
    
    /**
     * Affiche l'application principale après connexion réussie
     */
    private void showMainApplication() {
        try {
            // Obtenir l'instance de l'application principale
            Stage stage = (Stage) loginButton.getScene().getWindow();
            App app = (App) stage.getProperties().get("app");
            
            if (app != null) {
                app.showMainScreen();
            } else {
                // Fallback: fermer la fenêtre de login et ouvrir le dashboard
                Platform.runLater(() -> {
                    try {
                        javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                            getClass().getResource("/views/dashboard.fxml"));
                        javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
                        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
                        
                        stage.setScene(scene);
                        stage.setWidth(1200);
                        stage.setHeight(800);
                        stage.setResizable(true);
                        stage.centerOnScreen();
                        stage.setTitle("La Plateforme Tracker - Dashboard");
                        
                    } catch (Exception e) {
                        logger.error("Erreur lors de l'ouverture du dashboard", e);
                        showError("Impossible d'ouvrir l'application principale");
                    }
                });
            }
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'affichage de l'application principale", e);
            showError("Erreur lors de l'ouverture de l'application");
        }
    }
    
    /**
     * Active/désactive les contrôles de l'interface
     */
    private void setControlsEnabled(boolean enabled) {
        Platform.runLater(() -> {
            usernameField.setDisable(!enabled);
            passwordField.setDisable(!enabled);
            loginButton.setDisable(!enabled);
            cancelButton.setDisable(!enabled);
            
            if (enabled) {
                loginButton.setText("Se connecter");
            } else {
                loginButton.setText("Connexion...");
            }
        });
    }
    
    /**
     * Affiche un message d'erreur
     */
    private void showError(String message) {
        Platform.runLater(() -> {
            errorLabel.setText(message);
            errorLabel.setVisible(true);
            
            // Effacer le message après 5 secondes
            javafx.animation.Timeline timeline = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(5), 
                    e -> clearError())
            );
            timeline.play();
        });
    }
    
    /**
     * Efface le message d'erreur
     */
    private void clearError() {
        Platform.runLater(() -> {
            errorLabel.setVisible(false);
            errorLabel.setText("");
        });
    }
    
    /**
     * Gère l'affichage de l'écran d'inscription
     */
    @FXML
    private void handleShowRegister() {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/views/register.fxml"));
            javafx.scene.Scene scene = new javafx.scene.Scene(loader.load());
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            Stage stage = (Stage) loginButton.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Inscription - La Plateforme Tracker");
            stage.setResizable(false);
            stage.centerOnScreen();
            stage.show();
        } catch (Exception e) {
            showError("Impossible d'afficher le formulaire d'inscription");
        }
    }
}
