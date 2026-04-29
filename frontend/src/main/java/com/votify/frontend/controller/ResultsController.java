package com.votify.frontend.controller;

import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.stage.Stage;

import java.io.IOException;

// Controlador auxiliar que abre la pantalla de resultados.
public class ResultsController {
    // Navega a la vista de resultados o muestra error si falla.
    public void viewResults(Stage stage) {
        try {
            SceneNavigator.showScene(
                    stage,
                    "/com/votify/frontend/view/ResultsForm.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Resultados"
            );
        } catch (IOException e) {
            AlertHelper.showError("No se pudo abrir la pantalla de resultados: " + e.getMessage());
        }
    }
}
