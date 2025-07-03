package com.example.controller;

import com.example.App;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class RegisterController implements Initializable {
    @FXML private TextField usernameField;
    @FXML private PasswordField passwordField;
    @FXML private PasswordField confirmPasswordField;
    @FXML private TextField firstNameField;
    @FXML private TextField lastNameField;
    @FXML private TextField emailField;
    @FXML private Button registerButton;
    @FXML private Button cancelButton;
    @FXML private Label errorLabel;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Mode démo: pas de connexion réelle
    }

    @FXML
    private void handleRegister() {
        // Validation simplifiée
        if (usernameField.getText().trim().isEmpty() || passwordField.getText().isEmpty()
            || confirmPasswordField.getText().isEmpty()) {
            errorLabel.setText("Nom d'utilisateur et mot de passe obligatoires.");
            errorLabel.setVisible(true);
            return;
        }
        if (!passwordField.getText().equals(confirmPasswordField.getText())) {
            errorLabel.setText("Les mots de passe ne correspondent pas.");
            errorLabel.setVisible(true);
            return;
        }
        
        // Inscription réussie (stub)
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Inscription");
        alert.setHeaderText(null);
        alert.setContentText("Inscription réussie ! Vous pouvez vous connecter.");
        alert.showAndWait();
        
        handleCancel();
    }

    @FXML
    private void handleCancel() {
        try {
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            App app = (App) stage.getProperties().get("app");
            if (app != null) {
                app.showLoginScreen();
            }
        } catch (Exception e) {
            // Fallback: fermer
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
        }
    }
}
