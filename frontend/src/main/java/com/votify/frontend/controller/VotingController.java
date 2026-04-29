package com.votify.frontend.controller;

import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.stage.Stage;

import java.io.IOException;

// Controlador auxiliar que abre la pantalla de votación.
public class VotingController {
    // Navega al formulario de votación o muestra aviso si falla.
    public void performVoting(Stage stage) {
        try {
            SceneNavigator.showScene(
                    stage,
                    "/com/votify/frontend/view/VotingForm.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Votación"
            );
        } catch (IOException e) {
            AlertHelper.showWarning("No se pudo abrir la pantalla de votacion: " + e.getMessage());
        }
    }
}
