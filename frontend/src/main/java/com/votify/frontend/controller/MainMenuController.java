package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.client.AccessDecision;
import com.votify.frontend.client.AccessTarget;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import java.io.IOException;
import com.votify.frontend.navigation.SceneNavigator;

/**
 * Controlador para el menú principal de la aplicación de votación. Este controlador maneja las acciones de los botones
 * en la interfaz gráfica, como votar, registrar participantes, ver resultados y salir de la aplicación.
 * Utiliza instancias de VotingController, RegistrationController y ResultsController para delegar las acciones correspondientes.
 */

// Controlador del menú principal de la aplicación.
public class MainMenuController {
    private final VotingController votingController = new VotingController();
    private final RegistrationController registrationController = new RegistrationController();
    private final ResultsController resultsController = new ResultsController();
    private final VotifyApi apiClient = ApiClient.getInstance();

    @FXML
    private Button voteButton;

    @FXML
    private Button registerButton;

    @FXML
    private Button viewResultsButton;

    @FXML
    private Label voteSubtitle;

    @FXML
    private Label registerSubtitle;

    @FXML
    private Label resultsSubtitle;

    @FXML
    private Label userNameLabel;

    @FXML
    private Label pageTitleLabel;

    @FXML
    private Label pageSubtitleLabel;

    @FXML
    private HBox actionCardsBox;

    @FXML
    // Inicializa saludo, estado de conexión y acciones del menú.
    private void initialize() {
        boolean juryUser = apiClient.isCurrentUserJury();
        userNameLabel.setText(apiClient.getCurrentUserEmail());
        configureDashboardForRole(juryUser);
        try {
            AccessDecision votingAccess = apiClient.checkAccess(AccessTarget.VOTING);
            if (!votingAccess.allowed()) {
                voteButton.setDisable(true);
                if (voteSubtitle != null) voteSubtitle.setText(votingAccess.message());
            }

            AccessDecision registrationAccess = apiClient.checkAccess(AccessTarget.REGISTRATION);
            if (juryUser) {
                registerButton.setDisable(true);
                if (registerSubtitle != null) registerSubtitle.setText("El jurado solo emite valoraciones");
            } else if (registerButton != null && !registrationAccess.allowed()) {
                registerButton.setDisable(true);
                if (registerSubtitle != null) registerSubtitle.setText(registrationAccess.message());
            }

            AccessDecision resultsAccess = apiClient.checkAccess(AccessTarget.RESULTS);
            if (viewResultsButton != null) {
                if (!resultsAccess.allowed()) {
                    viewResultsButton.setDisable(true);
                    if (resultsSubtitle != null) resultsSubtitle.setText(resultsAccess.message());
                } else {
                    viewResultsButton.setDisable(false);
                    if (resultsSubtitle != null) resultsSubtitle.setText("Visualiza el ranking");
                }
            }
        } catch (ApiClientException e) {
            // No bloquear la UI, pero es útil registrar el error para depuración.
            System.err.println("No se pudo comprobar el estado del voto: " + e.getMessage());
        }
    }

    @FXML
    // Comprueba permisos y abre la pantalla de votación.
    private void vote() {
        if (checkConnection()) {
            try {
                AccessDecision access = apiClient.checkAccess(AccessTarget.VOTING);
                if (!access.allowed()) {
                    AlertHelper.showWarning(access.message());
                    return;
                }
                votingController.performVoting(currentStage());
            } catch (ApiClientException e) { AlertHelper.showError("Error: " + e.getMessage()); }
        }
    }

    @FXML
    // Comprueba permisos y abre la pantalla de registro.
    private void register() {
        if (checkConnection()) {
            try {
                AccessDecision access = apiClient.checkAccess(AccessTarget.REGISTRATION);
                if (!access.allowed()) {
                    AlertHelper.showWarning(access.message());
                    return;
                }
                registrationController.performRegistration(currentStage());
            } catch (ApiClientException e) { AlertHelper.showError("Error: " + e.getMessage()); }
        }
    }

    @FXML
    // Comprueba permisos y abre la pantalla de resultados.
    private void viewResults() {
        if (checkConnection()) {
            try {
                AccessDecision access = apiClient.checkAccess(AccessTarget.RESULTS);
                if (!access.allowed()) {
                    AlertHelper.showWarning(access.message());
                    return;
                }
                resultsController.viewResults(currentStage());
            } catch (ApiClientException e) { AlertHelper.showError("Error: " + e.getMessage()); }
        }
    }

    @FXML
    // Cierra la sesión local y vuelve a la pantalla de acceso.
    private void exit() {
        ApiClient.getInstance().logout();
        try {
            SceneNavigator.showScene(
                    currentStage(),
                    "/com/votify/frontend/view/Access.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Acceso"
            );
        } catch (IOException e) {
            AlertHelper.showError("Error al cerrar sesión: " + e.getMessage());
        }
    }

    // Obtiene el Stage actual desde un nodo de la pantalla.
    private Stage currentStage() {
        return (Stage) voteButton.getScene().getWindow();
    }

    // Comprueba si el backend responde a una llamada sencilla.
    private boolean checkConnection() {
        try {
            apiClient.getEventSettings();
            return true;
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
            return false;
        }
    }

    // Ajusta la pantalla principal al perfil público o jurado.
    private void configureDashboardForRole(boolean juryUser) {
        if (!juryUser) {
            return;
        }
        if (pageTitleLabel != null) {
            pageTitleLabel.setText("Panel de Jurado");
        }
        if (pageSubtitleLabel != null) {
            pageSubtitleLabel.setText("Evalúa proyectos participantes y consulta resultados en tiempo real");
        }
        if (registerButton != null) {
            registerButton.setVisible(false);
            registerButton.setManaged(false);
        }
        if (voteSubtitle != null) {
            voteSubtitle.setText("Evalúa proyectos participantes");
        }
        if (resultsSubtitle != null) {
            resultsSubtitle.setText("Visualiza el ranking");
        }
        if (actionCardsBox != null) {
            voteButton.setPrefWidth(544);
            viewResultsButton.setPrefWidth(544);
        }
    }
}
