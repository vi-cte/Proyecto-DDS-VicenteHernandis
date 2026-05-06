package com.votify.frontend.controller;

import com.votify.frontend.client.AccessDecision;
import com.votify.frontend.client.AccessTarget;
import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import com.votify.frontend.client.FormValidators;
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

// Controlador de la pantalla de acceso, registro y entrada admin.
public class AccessController {

    @FXML private Label loginTab;
    @FXML private Label registerTab;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private HBox forgotPasswordBox;
    @FXML private Button actionButton;
    @FXML private Button viewResultsButton;
    @FXML private Button adminSettingsButton;
    @FXML private Label errorLabel;

    private boolean isLoginMode = true;
    private final ApiClient authProxy = ApiClient.getInstance();

    @FXML
    // Configura acciones iniciales y comprueba la conexión con el backend.
    public void initialize() {
        showLogin(); 
        checkBackendConnection();

        // Listener para validar el correo al salir de la casilla (perder el foco)
        emailField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) { 
                String email = emailField.getText();
                if (!email.isBlank() && !FormValidators.isValidEmail(email)) {
                    showInlineError(FormValidators.MSG_INVALID_EMAIL);
                } else if (errorLabel != null && FormValidators.MSG_INVALID_EMAIL.equals(errorLabel.getText())) {
                    errorLabel.setText(""); // Limpia el error si se ha corregido
                }
            }
        });

        // Listener para validar la contraseña al salir de la casilla (solo en registro)
        passwordField.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue && !isLoginMode) { 
                String password = passwordField.getText();
                if (!password.isBlank() && password.length() < 8) {
                    showInlineError(FormValidators.MSG_SHORT_PASSWORD);
                } else if (errorLabel != null && FormValidators.MSG_SHORT_PASSWORD.equals(errorLabel.getText())) {
                    errorLabel.setText(""); // Limpia el error si se ha corregido
                }
            }
        });
    }

    // Valida si el backend está disponible al abrir la pantalla.
    private void checkBackendConnection() {
        try {
            // Obtenemos los ajustes del evento para comprobar la conexión y configurar la vista
            var settings = authProxy.getEventSettings();
            if (viewResultsButton != null) {
                if (!settings.isResultsVisible()) {
                    viewResultsButton.setDisable(true);
                    viewResultsButton.setText("Resultados aun no publicados");
                } else {
                    viewResultsButton.setDisable(false);
                    viewResultsButton.setText("Ver resultados votación");
                }
            }
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
    // Cambia el formulario al modo de inicio de sesión.
    private void showLogin() {
        if (errorLabel != null) errorLabel.setText("");
        isLoginMode = true;
        loginTab.getStyleClass().addAll("active-tab-login");
        registerTab.getStyleClass().removeAll("active-tab-register");
        
        forgotPasswordBox.setVisible(true);
        forgotPasswordBox.setManaged(true);
        actionButton.setText("Iniciar sesión");
    }

    @FXML
    // Cambia el formulario al modo de registro.
    private void showRegister() {
        if (errorLabel != null) errorLabel.setText("");
        isLoginMode = false;
        registerTab.getStyleClass().addAll("active-tab-register");
        loginTab.getStyleClass().removeAll("active-tab-login");
        
        forgotPasswordBox.setVisible(false);
        forgotPasswordBox.setManaged(false);
        actionButton.setText("Registrarse");
    }

    @FXML
    // Muestra la información de contacto para recuperar contraseña.
    private void handleForgotPassword() {
        AlertHelper.showInfo("Contacte con el administrador a traves del correo: admin@votify.com");
    }

    @FXML
    // Ejecuta login o registro según el modo activo.
    private void handleAction() {
        if (errorLabel != null) errorLabel.setText("");
        String email = emailField.getText(), password = passwordField.getText();
        if (email.isBlank() || password.isBlank()) { 
            showInlineError(FormValidators.MSG_REQUIRED_FIELDS); 
            return; 
        }

        if (!FormValidators.isValidEmail(email)) {
            showInlineError(FormValidators.MSG_INVALID_EMAIL);
            return;
        }

        if (!isLoginMode && password.length() < 8) {
            showInlineError(FormValidators.MSG_SHORT_PASSWORD);
            return;
        }

        try {
            if (isLoginMode) { authProxy.login(email, password); }
            else { authProxy.registerUser(email, password); AlertHelper.showInfo("Registro exitoso."); }
            
            Stage stage = (Stage) actionButton.getScene().getWindow();
            SceneNavigator.showMainMenu(stage);
        } catch (ApiClientException e) { 
            showInlineError(e.getMessage());
        } 
        catch (IOException e) { AlertHelper.showError("Error abriendo menú: " + e.getMessage()); }
    }

    // Muestra un error dentro de la pantalla de acceso.
    private void showInlineError(String message) {
        if (errorLabel != null) {
            errorLabel.setText(message);
            if (!errorLabel.getStyleClass().contains("access-error-label")) {
                errorLabel.getStyleClass().add("access-error-label");
            }
        } else {
            AlertHelper.showWarning(message); // Fallback si el FXML aún no tiene el Label
        }
    }

    @FXML
    // Permite consultar resultados desde la pantalla de acceso si están visibles.
    public void handleViewResults() {
        if (actionButton.isDisabled()) {
            AlertHelper.showError("No hay conexión con el servidor.");
            return;
        }
        try {
            AccessDecision access = authProxy.checkAccess(AccessTarget.RESULTS);
            if (!access.allowed()) {
                AlertHelper.showWarning(access.message());
                return;
            }
            Stage stage = (Stage) actionButton.getScene().getWindow();
            SceneNavigator.showScene(
                    stage,
                    "/com/votify/frontend/view/ResultsForm.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Resultados"
            );
        } catch (ApiClientException e) {
            AlertHelper.showError("Error comprobando el estado del evento: " + e.getMessage());
        } catch (IOException e) {
            AlertHelper.showError("Error abriendo resultados: " + e.getMessage());
        }
    }

    @FXML
    // Solicita contraseña admin y abre la pantalla de ajustes.
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
        pwd.setPromptText("admin123");

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
                    
                    // Refrescamos la vista para aplicar los cambios (ej. mostrar/ocultar el botón de resultados)
                    checkBackendConnection(); 
                } catch (IOException e) {
                    AlertHelper.showError("Error abriendo ajustes: " + e.getMessage());
                }
            } else {
                AlertHelper.showError("Contraseña incorrecta");
            }
        }
    }
}
