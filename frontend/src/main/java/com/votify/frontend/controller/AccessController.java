package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClientProxy;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.io.IOException;

public class AccessController {

    @FXML private Label loginTab;
    @FXML private Label registerTab;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private HBox forgotPasswordBox;
    @FXML private Button actionButton;

    private boolean isLoginMode = true;
    private final ApiClientProxy authProxy = ApiClientProxy.getInstance();

    @FXML
    public void initialize() { showLogin(); }

    @FXML
    private void showLogin() {
        isLoginMode = true;
        loginTab.getStyleClass().addAll("active-tab-login");
        registerTab.getStyleClass().removeAll("active-tab-register");
        
        forgotPasswordBox.setVisible(true);
        forgotPasswordBox.setManaged(true);
        actionButton.setText("Iniciar sesión");
    }

    @FXML
    private void showRegister() {
        isLoginMode = false;
        registerTab.getStyleClass().addAll("active-tab-register");
        loginTab.getStyleClass().removeAll("active-tab-login");
        
        forgotPasswordBox.setVisible(false);
        forgotPasswordBox.setManaged(false);
        actionButton.setText("Registrarse");
    }

    @FXML
    private void handleForgotPassword() {
        AlertHelper.showInfo("Contacte con el administrador a traves del correo: admin@votify.com");
    }

    @FXML
    private void handleAction() {
        String email = emailField.getText(), password = passwordField.getText();
        if (email.isBlank() || password.isBlank()) { AlertHelper.showWarning("Por favor, rellena todos los campos."); return; }

        try {
            if (isLoginMode) { authProxy.login(email, password); }
            else { authProxy.register(email, password); AlertHelper.showInfo("Registro exitoso."); }
            
            Stage stage = (Stage) actionButton.getScene().getWindow();
            SceneNavigator.showMainMenu(stage);
        } catch (ApiClientException e) { AlertHelper.showError(e.getMessage()); } 
        catch (IOException e) { AlertHelper.showError("Error abriendo menú: " + e.getMessage()); }
    }
}