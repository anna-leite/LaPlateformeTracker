package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Application JavaFX principale pour La Plateforme Tracker
 * Point d'entrée de l'application de gestion des étudiants
 */
public class App extends Application {
    private static final Logger logger = LoggerFactory.getLogger(App.class);
    
    private AppContext appContext;
    private Stage primaryStage;
    
    @Override
    public void init() throws Exception {
        super.init();
        
        try {
            // Initialiser le contexte de l'application
            appContext = AppContext.getInstance();
            appContext.initialize();
            
            logger.info("Application initialisée avec succès");
            
        } catch (Exception e) {
            logger.error("Erreur lors de l'initialisation de l'application", e);
            
            // Afficher une alerte d'erreur
            Platform.runLater(() -> {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Erreur d'initialisation");
                alert.setHeaderText("Impossible de démarrer l'application");
                alert.setContentText("Erreur: " + e.getMessage() + 
                    "\n\nVérifiez que PostgreSQL est démarré et que la base de données est accessible.");
                alert.showAndWait();
                Platform.exit();
            });
            
            throw e;
        }
    }
    
    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        // Expose App instance to controllers
        primaryStage.getProperties().put("app", this);
        
        try {
            // Configurer la fenêtre principale
            setupPrimaryStage();
            
            // Charger la liste des étudiants directement (pour développement)
            showStudentListDirect();
            
        } catch (Exception e) {
            logger.error("Erreur lors du démarrage de l'interface", e);
            showErrorAndExit("Erreur de démarrage", "Impossible de charger l'interface utilisateur", e);
        }
    }
    
    @Override
    public void stop() throws Exception {
        super.stop();
        
        try {
            if (appContext != null) {
                appContext.shutdown();
            }
            logger.info("Application fermée proprement");
        } catch (Exception e) {
            logger.error("Erreur lors de l'arrêt de l'application", e);
        }
    }
    
    /**
     * Configure la fenêtre principale de l'application
     */
    private void setupPrimaryStage() {
        primaryStage.setTitle("La Plateforme Tracker - Gestion des Étudiants");
        primaryStage.setMinWidth(800);
        primaryStage.setMinHeight(600);
        primaryStage.setMaximized(false);
        
        // Icône de l'application (optionnel)
        try {
            // primaryStage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        } catch (Exception e) {
            logger.warn("Impossible de charger l'icône de l'application");
        }
        
        // Gérer la fermeture de l'application
        primaryStage.setOnCloseRequest(event -> {
            try {
                if (appContext != null) {
                    appContext.shutdown();
                }
            } catch (Exception e) {
                logger.error("Erreur lors de la fermeture", e);
            }
            Platform.exit();
        });
    }
    
    /**
     * Affiche l'écran de connexion
     */
    public void showLoginScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/login.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Appliquer le CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setWidth(400);
            primaryStage.setHeight(300);
            primaryStage.setResizable(false);
            primaryStage.centerOnScreen();
            primaryStage.show();
            
            logger.info("Écran de connexion affiché");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de l'écran de connexion", e);
            showErrorAndExit("Erreur d'interface", "Impossible de charger l'écran de connexion", e);
        }
    }
    
    /**
     * Affiche l'écran principal de l'application après connexion
     */
    public void showMainScreen() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/dashboard.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Appliquer le CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            logger.info("Écran principal affiché");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de l'écran principal", e);
            showErrorAndExit("Erreur d'interface", "Impossible de charger l'écran principal", e);
        }
    }
    
    /**
     * Retourne à l'écran de connexion
     */
    public void showLoginScreenAgain() {
        showLoginScreen();
    }
    
    /**
     * Affiche directement la liste des étudiants (pour développement)
     */
    public void showStudentListDirect() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/student_list.fxml"));
            Scene scene = new Scene(loader.load());
            
            // Appliquer le CSS
            scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());
            
            primaryStage.setScene(scene);
            primaryStage.setWidth(1200);
            primaryStage.setHeight(800);
            primaryStage.setResizable(true);
            primaryStage.centerOnScreen();
            
            logger.info("Liste des étudiants affichée directement");
            
        } catch (Exception e) {
            logger.error("Erreur lors du chargement de la liste des étudiants", e);
            showErrorAndExit("Erreur d'interface", "Impossible de charger la liste des étudiants", e);
        }
    }
    
    /**
     * Affiche une erreur et ferme l'application
     */
    private void showErrorAndExit(String title, String header, Exception e) {
        Platform.runLater(() -> {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle(title);
            alert.setHeaderText(header);
            alert.setContentText("Erreur: " + e.getMessage());
            alert.showAndWait();
            Platform.exit();
        });
    }
    
    /**
     * Obtient le stage principal de l'application
     */
    public Stage getPrimaryStage() {
        return primaryStage;
    }
    
    /**
     * Point d'entrée de l'application
     */
    public static void main(String[] args) {
        // Configuration du système de logging
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");
        
        logger.info("Démarrage de La Plateforme Tracker...");
        
        try {
            launch(args);
        } catch (Exception e) {
            logger.error("Erreur fatale lors du démarrage de l'application", e);
            System.exit(1);
        }
    }
}
