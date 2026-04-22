package com.votify.frontend.controller;

import com.votify.frontend.navigation.SceneNavigator;
import com.votify.frontend.ui.AlertHelper;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {
    public void performRegistration(Stage stage) {
        try {
            SceneNavigator.showScene(
                    stage,
                    "/com/votify/frontend/view/RegistrationForm.fxml",
                    "/com/votify/frontend/view/MainMenu.css",
                    "Votify - Registro"
            );
        } catch (IOException e) {
            AlertHelper.showError("No se pudo abrir la pantalla de registro: " + e.getMessage());
        }
    }
}
