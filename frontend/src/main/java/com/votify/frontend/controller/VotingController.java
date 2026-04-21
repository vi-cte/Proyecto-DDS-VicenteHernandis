package com.votify.frontend.controller;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Objects;

public class VotingController {
    public void performVoting(Stage stage) {
        try {
            FXMLLoader loader = new FXMLLoader(VotingController.class.getResource("/com/votify/frontend/view/VotingForm.fxml"));
            Scene currentScene = stage.getScene();
            double width = currentScene == null ? 1365 : currentScene.getWidth();
            double height = currentScene == null ? 768 : currentScene.getHeight();
            Scene scene = new Scene(loader.load(), width, height);
            scene.getStylesheets().add(Objects.requireNonNull(
                    VotingController.class.getResource("/com/votify/frontend/view/VotingForm.css")
            ).toExternalForm());
            stage.setTitle("Votify - Votación");
            stage.setScene(scene);
            stage.show();
        } catch (IOException e) {
            Alert alert = new Alert(Alert.AlertType.WARNING);
            alert.setContentText("No se pudo abrir la pantalla de votacion: " + e.getMessage());
            alert.showAndWait();
        }
    }
}
