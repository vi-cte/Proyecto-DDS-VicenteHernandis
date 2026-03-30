package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class RegistrationController {
    public void performRegistration() {
        try {
            FXMLLoader loader = new FXMLLoader(RegistrationController.class.getResource("/com/votify/frontend/view/RegistrationForm.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Registrar participante");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            Alert error = new Alert(Alert.AlertType.ERROR);
            error.setContentText("No se pudo abrir la pantalla de registro: " + e.getMessage());
            error.showAndWait();
        }
    }
}
