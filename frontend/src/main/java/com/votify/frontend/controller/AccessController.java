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
import javafx.scene.control.Dialog;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ButtonBar;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.stage.Modality;
import javafx.scene.Scene;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.application.Platform;

import java.io.IOException;
import java.util.Optional;

public class AccessController {

    @FXML private Label loginTab;
    @FXML private Label registerTab;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private HBox forgotPasswordBox;
    @FXML private Button actionButton;
    @FXML private Button viewResultsButton;
    @FXML private Button adminSettingsButton;

    private boolean isLoginMode = true;
    private final ApiClientProxy authProxy = ApiClientProxy.getInstance();

    @FXML
    public void initialize() { 
        showLogin(); 
        checkBackendConnection();
    }

    private void checkBackendConnection() {
        try {
            // Intentamos hacer una petición pública rápida para comprobar si el servidor responde
            authProxy.getResults(); 
        } catch (ApiClientException e) {
            actionButton.setDisable(true);
            emailField.setDisable(true);
            passwordField.setDisable(true);
            loginTab.setDisable(true);
            registerTab.setDisable(true);
            viewResultsButton.setDisable(true);
            adminSettingsButton.setDisable(true);
            forgotPasswordBox.setDisable(true);
            Platform.runLater(() -> AlertHelper.showError("No se pudo conectar con el servidor backend. Verifica que esté iniciado."));
        }
    }

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

    @FXML
    public void handleViewResults() {
        if (actionButton.isDisabled()) {
            AlertHelper.showError("No hay conexión con el servidor.");
            return;
        }
        try {
            Stage stage = (Stage) actionButton.getScene().getWindow();
            SceneNavigator.showScene(
                    stage,
                    "/com/votify/frontend/view/ResultsForm.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Resultados"
            );
        } catch (IOException e) {
            AlertHelper.showError("Error abriendo resultados: " + e.getMessage());
        }
    }

    @FXML
    public void handleAdminSettings() {
        if (actionButton.isDisabled()) {
            AlertHelper.showError("No hay conexión con el servidor.");
            return;
        }
        Dialog<String> dialog = new Dialog<>();
        dialog.setTitle("Acceso Administrador");
        dialog.setHeaderText("Ajustes de Votación");
        
        ButtonType okButtonType = new ButtonType("Aceptar", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(okButtonType, ButtonType.CANCEL);

        PasswordField pwd = new PasswordField();
        pwd.setPromptText("Contraseña");

        VBox vbox = new VBox(10);
        vbox.getChildren().addAll(new Label("Introduce la contraseña de administrador:"), pwd);
        dialog.getDialogPane().setContent(vbox);

        Platform.runLater(pwd::requestFocus);

        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == okButtonType) {
                return pwd.getText();
            }
            return null;
        });

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            if (authProxy.authenticateAdmin(result.get())) {
                try {
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    Parent root = FXMLLoader.load(getClass().getResource("/com/votify/frontend/view/SettingsForm.fxml"));
                    stage.setScene(new Scene(root));
                    stage.setTitle("Configuración del evento");
                    stage.showAndWait();
                } catch (IOException e) {
                    AlertHelper.showError("Error abriendo ajustes: " + e.getMessage());
                }
            } else {
                AlertHelper.showError("Contraseña incorrecta");
            }
        }
    }
}