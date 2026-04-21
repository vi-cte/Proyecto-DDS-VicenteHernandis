package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class RegistrationController {
    public void performRegistration(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(RegistrationController.class.getResource("/com/votify/frontend/view/RegistrationForm.fxml"));
            Scene currentScene = stage.getScene();
            double width = currentScene == null ? 1365 : currentScene.getWidth();
            double height = currentScene == null ? 768 : currentScene.getHeight();
            Scene scene = new Scene(loader.load(), width, height);

            // Cargar la hoja de estilos principal y la específica del formulario
            scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/com/votify/frontend/view/MainMenu.css")).toExternalForm());

            stage.setTitle("Votify - Registro");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("No se pudo abrir la pantalla de registro: " + e.getMessage());
            error.showAndWait();
        }
    }
}
