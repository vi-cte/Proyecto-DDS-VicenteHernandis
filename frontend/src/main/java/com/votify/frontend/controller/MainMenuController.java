package com.votify.frontend.controller;

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

    @FXML
    private Button voteButton;

    @FXML
    private void vote() {
        Stage stage = (Stage) voteButton.getScene().getWindow();
        votingController.performVoting(stage);
    }

    @FXML
    private void register() {
        Stage stage = (Stage) voteButton.getScene().getWindow();
        registrationController.performRegistration(stage);
    }

    @FXML
    private void viewResults() {
        Stage stage = (Stage) voteButton.getScene().getWindow();
        resultsController.viewResults(stage);
    }

    @FXML
    private void exit() {
        System.exit(0);
    }
}
