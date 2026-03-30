package com.votify.frontend.controller;

import javafx.fxml.FXML;

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
    private void vote() {
        votingController.performVoting();
    }

    @FXML
    private void register() {
        registrationController.performRegistration();
    }

    @FXML
    private void viewResults() {
        resultsController.viewResults();
    }

    @FXML
    private void exit() {
        System.exit(0);
    }
}
