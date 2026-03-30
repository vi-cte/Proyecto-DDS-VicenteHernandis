package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;

public class VotingController {
    public void performVoting() {
        try {
            FXMLLoader loader = new FXMLLoader(VotingController.class.getResource("/com/votify/frontend/view/VotingForm.fxml"));
            Scene scene = new Scene(loader.load());

            Stage stage = new Stage();
            stage.setTitle("Votacion");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(scene);
            stage.showAndWait();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("No se pudo abrir la pantalla de votacion: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
