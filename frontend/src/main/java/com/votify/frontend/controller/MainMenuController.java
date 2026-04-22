package com.votify.frontend.controller;

import com.votify.frontend.client.ApiClient;
import com.votify.frontend.exception.ApiClientException;
import com.votify.frontend.ui.AlertHelper;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.stage.Stage;

/**
 * Controlador para el menú principal de la aplicación de votación. Este controlador maneja las acciones de los botones
 * en la interfaz gráfica, como votar, registrar participantes, ver resultados y salir de la aplicación.
 * Utiliza instancias de VotingController, RegistrationController y ResultsController para delegar las acciones correspondientes.
 */

public class MainMenuController {
    private final VotingController votingController = new VotingController();
    private final RegistrationController registrationController = new RegistrationController();
    private final ResultsController resultsController = new ResultsController();
    private final ApiClient apiClient = new ApiClient();

    @FXML
    private Button voteButton;

    @FXML
    private void vote() {
        if (checkConnection()) {
            votingController.performVoting(currentStage());
        }
    }

    @FXML
    private void register() {
        if (checkConnection()) {
            registrationController.performRegistration(currentStage());
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
        System.exit(0);
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
