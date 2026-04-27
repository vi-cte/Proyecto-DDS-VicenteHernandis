package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClientProxy;
import com.votify.frontend.client.VotifyApi;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import java.io.IOException;
import com.votify.frontend.navigation.SceneNavigator;

/**
 * Controlador para el menú principal de la aplicación de votación. Este controlador maneja las acciones de los botones
 * en la interfaz gráfica, como votar, registrar participantes, ver resultados y salir de la aplicación.
 * Utiliza instancias de VotingController, RegistrationController y ResultsController para delegar las acciones correspondientes.
 */

public class MainMenuController {
    private final VotingController votingController = new VotingController();
    private final RegistrationController registrationController = new RegistrationController();
    private final ResultsController resultsController = new ResultsController();
    private final VotifyApi apiClient = ApiClientProxy.getInstance();

    @FXML
    private Button voteButton;

    @FXML
    private Label userNameLabel;

    @FXML
    private void initialize() {
        userNameLabel.setText(apiClient.getCurrentUserEmail());
        try {
            if (apiClient.hasVoted()) {
                voteButton.setDisable(true);
                voteButton.setText("Ya has votado");
            }
        } catch (ApiClientException e) {
            // No bloquear la UI, pero es útil registrar el error para depuración.
            System.err.println("No se pudo comprobar el estado del voto: " + e.getMessage());
        }
    }

    @FXML
    private void vote() {
        if (checkConnection()) {
            try {
                if (!apiClient.getEventSettings().isVotingOpen()) {
                    AlertHelper.showWarning("Las votaciones están cerradas actualmente.");
                    return;
                }
                votingController.performVoting(currentStage());
            } catch (ApiClientException e) { AlertHelper.showError("Error: " + e.getMessage()); }
        }
    }

    @FXML
    private void register() {
        if (checkConnection()) {
            try {
                if (!apiClient.getEventSettings().isRegistrationsOpen()) {
                    AlertHelper.showWarning("Las inscripciones están cerradas actualmente.");
                    return;
                }
                registrationController.performRegistration(currentStage());
            } catch (ApiClientException e) { AlertHelper.showError("Error: " + e.getMessage()); }
        }
    }

    @FXML
    private void viewResults() {
        if (checkConnection()) {
            resultsController.viewResults(currentStage());
        }
    }

    @FXML
    private void exit() {
        ApiClientProxy.getInstance().logout();
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

    private Stage currentStage() {
        return (Stage) voteButton.getScene().getWindow();
    }

    private boolean checkConnection() {
        try {
            apiClient.getVotingLimit();
            return true;
        } catch (ApiClientException e) {
            AlertHelper.showError(e.getMessage());
            return false;
        }
    }
}
